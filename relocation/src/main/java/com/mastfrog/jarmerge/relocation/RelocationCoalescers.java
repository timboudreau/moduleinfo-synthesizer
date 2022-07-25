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

    public RelocationCoalescers(ClassRelocatingJarFilter owner) {
        this.owner = owner;
    }

    void writeCoalesced(JarOutputStream out, MergeLog log, RelocationEntry entry,
            IOSupplier<InputStream> originalBytes) throws Exception {

        JarEntry je = newJarEntry(entry.newJarEntryName());
        out.putNextEntry(je);
        try {
            out.write(applyTransforms(originalBytes));
        } finally {
            out.closeEntry();
        }
    }

    void writeCoalesced(JarOutputStream out, MergeLog log, AnyCoalescer entry,
            IOSupplier<InputStream> originalBytes) throws Exception {
        JarEntry je = newJarEntry(entry.path());
        out.putNextEntry(je);
        try {
            out.write(applyTransforms(originalBytes));
        } finally {
            out.closeEntry();
        }
    }

    private byte[] applyTransforms(IOSupplier<InputStream> on) throws Exception {
        try ( InputStream in = on.get()) {
            ClassReader cr = new ClassReader(in);
            ClassWriter cw = new ClassWriter(0);
            ClassRemapperImpl glarg = new ClassRemapperImpl(cw);
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

        public ClassRemapperImpl(ClassVisitor classVisitor) {
            this(classVisitor, new Remap());
        }

        private ClassRemapperImpl(ClassVisitor cv, Remap remap) {
            super(cv, remap);
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                String superName, String[] interfaces) {
            if (interfaces != null) {
                String[] nue = new String[interfaces.length];
                for (int i = 0; i < interfaces.length; i++) {
                    nue[i] = remap(interfaces[i]);
                }
                interfaces = nue;
            }
            super.visit(version, access, remap(name), remap(signature), remap(superName), interfaces);
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
            super.visitOuterClass(remap(owner), remap(name), remap(descriptor));
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

        @Override
        public String map(String internalName) {
            String nt = remap(internalName);
            return super.map(nt);
        }

        @Override
        public Object mapValue(Object value) {
            if (value instanceof String) {
                if (looksLikeFQN((String) value)) {
                    String v = ((String) value).replace('.', '/');
                    String nue = remap(v);
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
            return super.mapTypes(remap(internalNames));
        }

        @Override
        public String mapDesc(String descriptor) {
            String nt = remap(descriptor);
            return super.mapDesc(nt);
        }

        @Override
        public String mapType(String internalName) {
            String nt = remap(internalName);
            return super.mapType(nt);
        }

        @Override
        public String mapInnerClassName(String name, String ownerName, String innerName) {
            return super.mapInnerClassName(remap(name), remap(ownerName), innerName);
        }

        @Override
        public String mapSignature(String signature, boolean typeSignature) {
            String orig = signature;
            if (signature != null) {
                String nue = remap(signature);
                signature = nue;
            }
            try {
                return super.mapSignature(signature, typeSignature);
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException iae) {
                throw new IllegalArgumentException("Bad signature remap '"
                        + orig + "' -> '" + signature + "'", iae);
            }
        }

        @Override
        public String mapFieldName(String owner, String name, String descriptor) {
            return super.mapFieldName(remap(owner), name, descriptor);
        }

        @Override
        public String mapRecordComponentName(String owner, String name, String descriptor) {
            return super.mapRecordComponentName(remap(owner), name, descriptor);
        }

        @Override
        public String mapAnnotationAttributeName(String descriptor, String name) {
            return super.mapAnnotationAttributeName(remap(descriptor), remap(name));
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
