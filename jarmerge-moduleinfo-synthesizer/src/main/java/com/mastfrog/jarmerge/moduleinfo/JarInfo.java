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
package com.mastfrog.jarmerge.moduleinfo;

import com.mastfrog.function.throwing.io.IOFunction;
import com.mastfrog.jarmerge.MergeLog;
import com.mastfrog.util.path.UnixPath;
import java.io.IOException;
import java.io.InputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.Method;
import org.netbeans.modules.classfile.Module;

/**
 * Collection of data about packages and services discovered from one JAR.
 *
 * @author Tim Boudreau
 */
final class JarInfo implements Comparable<JarInfo> {

    private final Map<String, Set<String>> serviceData = new HashMap<>();
    private final Set<String> packages = new HashSet<>();
    private final Path jar;
    private ClassFile moduleFile;
    String moduleName;
    private int moduleFlags = 0;

    JarInfo(Path jar) {
        this.jar = jar;
    }

    @Override
    public String toString() {
        return jar + " (" + packages + " + " + serviceData + ")";
    }

    private Set<String> services(String type) {
        return serviceData.computeIfAbsent(type, t -> new LinkedHashSet<>());
    }

    void collect(JarsData data) {
        if (moduleFile != null) {
            Module mod = moduleFile.getModule();
            if (mod != null) {
                data.encounterModule(mod.getName(), mod.getVersion());
                mod.getExportsEntries().forEach(data::encounterExport);
                mod.getOpensEntries().forEach(data::encounterOpen);
                mod.getUses().forEach(u -> {
                    data.encounterUse(u.getExternalName());
                });
                mod.getProvidesEntries().forEach(data::encounterProvides);
                mod.getRequiresEntries().forEach(data::encounterRequires);
                serviceData.forEach((type, impls)
                        -> impls.forEach(impl -> data.encounterProvides(type, impl)));
                return;
            }
        }
        serviceData.forEach((type, impls)
                -> impls.forEach(impl -> data.encounterProvides(type, impl)));
        packages.forEach(data::nonModuleExport);
    }

    boolean hasModuleInfo() {
        return moduleName != null;
    }

    private String rawName() {
        String name = jar.getFileName().toString();
        int ix = name.lastIndexOf('.');
        if (ix > 0) {
            name = name.substring(0, ix);
        }
        return name;
    }

    void readModuleInfo(JarEntry entry, JarFile file, InputStream in, MergeLog log) throws IOException {
        ClassFile cf = new ClassFile(in);
        org.netbeans.modules.classfile.Module mod = cf.getModule();
        moduleName = mod.getName();
        moduleFlags = mod.getFlags();
        moduleFile = cf;
    }

    boolean note(JarEntry e) {
        UnixPath up = UnixPath.get(e.getName());
        UnixPath par = up.getParent();
        if (par != null && !par.toString().contains("META-INF")) {
            packages.add(par.toString('.'));
        }
        return false;
    }

    @Override
    public int compareTo(JarInfo o) {
        return rawName().compareToIgnoreCase(o.rawName());
    }

    void readServiceFile(String service, JarFile file, JarEntry entry,
            InputStream in, MergeLog log, boolean checkServiceConstructors) throws IOException {
        Set<String> svcs = services(service);
        String content = new String(in.readAllBytes(), UTF_8);
        String[] lines = content.split("\n");
        for (String l : lines) {
            l = l.trim();
            if (l.isEmpty() || l.charAt(0) == '#') {
                continue;
            }
            // Some hadoop jars hack ServiceLoader to get things instantiated
            // with arguments - this will never work in the module system, so
            // omit such unsound service registrations
            if (!checkServiceConstructors || hasDefaultConstructor(service, file, l)) {
                svcs.add(l);
            }
        }
    }

    private boolean hasDefaultConstructor(String service, JarFile file, String implementationClass) throws IOException {
        Boolean result = withPermutations(service, (classFilePath) -> {
            ZipEntry en = file.getEntry(service.replace('.', '/') + ".class");
            if (en != null) {
                try ( InputStream in = file.getInputStream(en)) {
                    ClassFile cf = new ClassFile(in);
                    for (Method m : cf.getMethods()) {
                        if (m.isPublic()) {
                            if ("<init>".equals(m.getName())) {
                                return true;
                            }
                        }
                    }
                }
            }
            // null = keep trying other permutations
            return null;
        });
        return result;
    }

    private static boolean withPermutations(String serviceClassName, IOFunction<String, Boolean> f) throws IOException {
        // There are a few legal forms a class name could be transmuted into - such ass
        // com.foo.MyClass.MyNestedClass -> com.foo.MyClass$MyNestedClass which would
        // be com/foo/MyClass$MyNestedClass in the jar
        List<String> all = new ArrayList<>(3);
        String baseSlashes = serviceClassName.replace('.', '/');
        all.add(baseSlashes + ".class");
        int ix = baseSlashes.lastIndexOf('.');
        if (ix > 0 && ix < baseSlashes.length() - 1) {
            char[] c = baseSlashes.toCharArray();
            c[ix] = '$';
            all.add(new String(c) + ".class");
        }
        for (String s : all) {
            Boolean result = f.apply(s);
            if (result != null) {
                return result;
            }
        }
        return false;
    }
}
