/*
 * The MIT License
 *
 * Copyright 2022 Mastfrog Technologies.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mastfrog.jarmerge.relocation;

import static com.mastfrog.asmgraph.Parsing.classSignature;
import static com.mastfrog.asmgraph.Parsing.fieldSignature;
import static com.mastfrog.asmgraph.Parsing.methodSignature;
import com.mastfrog.asmgraph.asm.model.ClassSignature;
import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.function.throwing.io.IOSupplier;
import com.mastfrog.jarmerge.MergeLog;
import static com.mastfrog.jarmerge.relocation.TypeNameUtils.looksLikeFQN;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/**
 * Aggregates a set of renames and associated coalescers.
 *
 * @author Tim Boudreau
 */
final class RelocationCoalescers {

    private static final FileTime EPOCH = FileTime.fromMillis(0);
    private final Map<String, Set<RelocationEntry>> entries = new ConcurrentHashMap<>();
    private final Set<RelocationEntry> all = ConcurrentHashMap.newKeySet();
    private final Set<AnyCoalescer> anys = ConcurrentHashMap.newKeySet();
    private final ClassRelocatingJarFilter owner;
    private final Set<String> noRemapCache = new HashSet<>();

    Set<String> writtenEntries = new HashSet<>();

    public RelocationCoalescers(ClassRelocatingJarFilter owner) {
        this.owner = owner;
    }

    void writeCoalesced(JarOutputStream out, MergeLog log, RelocationEntry entry,
            IOSupplier<InputStream> originalBytes) throws Exception {
        String ne = entry.newJarEntryName();
        if (writtenEntries.add(ne)) {
            JarEntry je = newJarEntry(entry.newJarEntryName());
            out.putNextEntry(je);
            try {
                out.write(applyTransforms(entry.in(), entry.path(), originalBytes));
            } finally {
                out.closeEntry();
            }
        }
    }

    void writeCoalesced(JarOutputStream out, MergeLog log, AnyCoalescer entry,
            IOSupplier<InputStream> originalBytes) throws Exception {
        String ne = entry.path();
        if (writtenEntries.add(ne)) {
            JarEntry je = newJarEntry(entry.path());
            out.putNextEntry(je);
            try {
                out.write(applyTransforms(entry.jar, entry.path, originalBytes));
            } catch (IllegalArgumentException ex) {
                // Okay, this is just awful:
                // ASM will emit this valid signature for a method:
                // ()Lorg/apache/hadoop/thirdparty/com/google/common/collect/StandardTable<TR;TC;TV;>.Row;Ljava/util/SortedMap<TC;TV;>;
                // However, its internal SignatureReader cannot parse it.  So, effectively,
                // we simply cannot relocate classes that contain a signature like this.
                //
                // Dangerous, but the best we can do is to write the unrelocated bytes
                // and hope it is not a type we actually want to relocate.
                ex.printStackTrace();
                out.write(originalBytes.get().readAllBytes());
            } finally {
                out.closeEntry();
            }
        }
    }

    private byte[] applyTransforms(Path jar, String path, IOSupplier<InputStream> on) throws Exception {
        try ( InputStream in = on.get()) {
            ClassReader cr = new ClassReader(in);
            ClassWriter cw = new ClassWriter(0);
            ClassRemapperImpl glarg = new ClassRemapperImpl(jar, path, cw);
            cr.accept(glarg, ClassReader.EXPAND_FRAMES);
            return cw.toByteArray();
        }
    }

    private static boolean isLSemi(String what) {
        return what.length() > 2 && what.charAt(0) == 'L' && what.charAt(what.length() - 1) == ';';
    }

    private static String lSemi(String what) {
        return 'L' + what + ';';
    }

    private String simpleRemap(String what) {
        boolean is = isLSemi(what);
        if (is) {
            what = what.substring(1, what.length() - 1);
        }
        Set<RelocationEntry> es = entries.getOrDefault(what, java.util.Collections.emptySet());
        if (es.isEmpty()) {
            return is ? lSemi(what) : what;
        }
        String result = stripDotClass(es.iterator().next().newJarEntryName());
        return is ? lSemi(result) : result;
    }

    String remapMethodSignature(String signature) {
        if (signature == null) {
            return null;
        }
        if (noRemapCache.contains(signature)) {
            return signature;
        }
        MethodSignature sig = methodSignature(signature);
        return debugRemap(signature, sig.transform(this::simpleRemap).toString(), "method");
    }

    String remapType(String signature) {
        if (signature == null) {
            return null;
        }
        if (noRemapCache.contains(signature)) {
            return signature;
        }
        if (signature.startsWith("L") && signature.endsWith(";")) {
            TypeName sig = fieldSignature(signature);
            return debugRemap(signature, sig.transform(this::simpleRemap).toString(), "typeFull");
        } else {
            TypeName nm = TypeName.simpleName(signature);
            return debugRemap(signature, nm.transform(this::simpleRemap).toString(), "typeSimple");
        }
    }

    String remapClassSignature(String classSignature) {
        if (classSignature == null) {
            return null;
        }
        if (noRemapCache.contains(classSignature)) {
            return classSignature;
        }
        if (classSignature.startsWith("L") && classSignature.endsWith(";")) {
            return debugRemap(classSignature, remapType(classSignature), "classAsSimpleType");
        }
        if (classSignature.indexOf('(') >= 0) {
            return debugRemap(classSignature, remapMethodSignature(classSignature), "classAsMethodSig");
        }
        ClassSignature sig = classSignature(classSignature);
        return debugRemap(classSignature, sig.transform(this::simpleRemap).toString(), "class");
    }

    private String debugRemap(String sig, String output, String kind) {
        // This saves a LOT of parsing
        if (sig.equals(output)) {
            noRemapCache.add(sig);
        }
        return output;
    }

    public String remap(String what) {
        if (what == null) {
            return null;
        }
        boolean trailingV = what.endsWith(")V");
        if (trailingV) {
            what = what.substring(0, what.length() - 1);
        }
        String result = TypeNameUtils.remapNested(what, this::simpleRemap);
        if (trailingV) {
            result += "V";
        }
        return result.replaceAll(",;", ";").replaceAll("\\(>", ">(");
    }

    public String remapPackage(String pkg) {
        return owner.replacementPackage(pkg);
    }

    public String[] remap(String[] what) {
        if (what == null) {
            return null;
        }
        String[] nue = new String[what.length];
        for (int i = 0; i < what.length; i++) {
            nue[i] = remap(what[i]);
        }
        return nue;
    }

    public String[] remapTypes(String[] what) {
        if (what == null) {
            return null;
        }
        String[] nue = new String[what.length];
        for (int i = 0; i < what.length; i++) {
            nue[i] = remapType(what[i]);
        }
        return nue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(entries.size() + " relocs "
                + anys.size() + " anys");
        entries.forEach((k, v) -> {
            sb.append("\nR ").append(k).append(" ").append(v);
        });
        anys.forEach(a -> {
            sb.append("\nA ").append(a);
        });
        return sb.append('\n').toString();
    }

    private final class ClassRemapperImpl extends ClassRemapper {

        private final Path jar;
        private final String entry;

        public ClassRemapperImpl(Path jar, String entry, ClassVisitor classVisitor) {
            this(jar, entry, classVisitor, new Remap(jar, entry));
        }

        private ClassRemapperImpl(Path jar, String entry, ClassVisitor cv, Remap remap) {
            super(cv, remap);
            this.jar = jar;
            this.entry = entry;
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                String superName, String[] interfaces) {
            super.visit(version, access, remap(name), remapMethodSignature(signature),
                    remapType(superName), remapTypes(interfaces));
        }

        @Override
        public void visitNestMember(String nestMember) {
            super.visitNestMember(remap(nestMember));
        }

        @Override
        public void visitNestHost(String nestHost) {
            super.visitNestHost(remap(nestHost));
        }

        @Override
        public void visitOuterClass(String owner, String name, String descriptor) {
            super.visitOuterClass(remapType(owner), remap(name), remapMethodSignature(descriptor));
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
        }

        @Override
        public void visitSource(final String source, final String debug) {
            if (source == null) {
                super.visitSource(null, debug);
                return;
            }
            super.visitSource(remap(source), debug);
        }
    }

    private final class Remap extends Remapper {

        private final Path jar;
        private final String entry;

        Remap(Path jar, String entry) {
            this.jar = jar;
            this.entry = entry;
        }

        @Override
        public String map(String internalName) {
            String nt = remapType(internalName);
            return super.map(nt);
        }

        @Override
        public Object mapValue(Object value) {
            if (value instanceof String) {
                if (looksLikeFQN((String) value)) {
                    String v = ((String) value).replace('.', '/');
                    String nue = remapType(v);
                    if (!v.equals(nue)) {
                        value = stripDotClass(nue).replace('/', '.');
                    }
                }
            }
            return super.mapValue(value);
        }

        @Override
        public String mapPackageName(String name) {
            return super.mapPackageName(remapPackage(name));
        }

        @Override
        public String[] mapTypes(String[] internalNames) {
            return super.mapTypes(remapTypes(internalNames));
        }

        @Override
        public String mapDesc(String descriptor) {
            String nt;
            if (descriptor.indexOf('(') >= 0) {
                nt = remapMethodSignature(descriptor);
            } else {
                nt = remapType(descriptor);
            }
            return super.mapDesc(nt);
        }

        @Override
        public String mapType(String internalName) {
            String nt = remapType(internalName);
            return super.mapType(nt);
        }

        @Override
        public String mapInnerClassName(String name, String ownerName, String innerName) {
            return super.mapInnerClassName(remap(name), remapType(ownerName), innerName);
        }

        @Override
        public String mapSignature(String signature, boolean typeSignature) {
            String orig = signature;
            if (signature != null) {
                String nue = typeSignature ? remapClassSignature(signature) : remapMethodSignature(signature);
                signature = nue;
            }
            try {
                return super.mapSignature(signature, typeSignature);
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException iae) {
//                throw new IllegalArgumentException("Bad signature remap '"
//                        + orig + "' -> '" + signature + "'", iae);
                IllegalArgumentException iae2 = new IllegalArgumentException("Bad signature remap '"
                        + orig + "' -> '" + signature + "' in " + entry + " of " + jar.getFileName(), iae);
                iae2.printStackTrace();
                try {
                    return super.mapSignature(orig, typeSignature);
                } catch (IllegalArgumentException iae3) {
                    iae2.addSuppressed(iae3);
                    if (orig.indexOf('.') > 0) {
                        try {
                            return super.mapSignature(orig.replace('.', '/'), typeSignature);
                        } catch (IllegalArgumentException iae4) {
                            iae2.addSuppressed(iae4);
                            throw iae2;
                        }
                    } else {
                        throw iae2;
                    }
                }
            }
        }

        @Override
        public String mapFieldName(String owner, String name, String descriptor) {
            return super.mapFieldName(remapType(owner), name, descriptor);
        }

        @Override
        public String mapRecordComponentName(String owner, String name, String descriptor) {
            return super.mapRecordComponentName(remapType(owner), name, descriptor);
        }

        @Override
        public String mapAnnotationAttributeName(String descriptor, String name) {
            return super.mapAnnotationAttributeName(remapClassSignature(descriptor), remap(name));
        }
    }

    private JarEntry maybeZeroDates(JarEntry je) {
        if (owner.isZeroDates()) {
            je.setTime(0);
            je.setCreationTime(EPOCH);
            je.setLastAccessTime(EPOCH);
            je.setLastModifiedTime(EPOCH);
        }
        return je;
    }

    private JarEntry newJarEntry(String path) {
        return maybeZeroDates(new JarEntry(path));
    }

    AnyCoalescer addPath(String path, JarEntry entry, Path jar) {
        AnyCoalescer any = new AnyCoalescer(path, entry, jar, this);
        anys.add(any);
        return any;
    }

    private static String stripDotClass(String s) {
        if (s.endsWith(".class")) {
            int ix = s.lastIndexOf('.');
            return s.substring(0, ix);
        }
        return s;
    }

    RelocationCoalescer add(String path, String newPackage, JarEntry entry, Path jar) {
        Set<RelocationEntry> en = entries.computeIfAbsent(stripDotClass(path), p -> new HashSet<>());
        RelocationEntry nue = new RelocationEntry(newPackage, path, jar);
        en.add(nue);
        all.add(nue);
        RelocationCoalescer result = new RelocationCoalescer(nue, this);
        return result;
    }
}
