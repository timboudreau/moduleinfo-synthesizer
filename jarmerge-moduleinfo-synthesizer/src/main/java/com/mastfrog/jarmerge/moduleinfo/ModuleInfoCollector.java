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

import com.mastfrog.jarmerge.MergeLog;
import com.mastfrog.jarmerge.builtin.ConcatenateMetaInfServices;
import com.mastfrog.jarmerge.spi.Coalescer;
import com.mastfrog.util.collections.CollectionUtils;
import com.mastfrog.util.file.FileUtils;
import com.mastfrog.util.path.UnixPath;
import com.mastfrog.util.streams.Streams;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 *
 * @author Tim Boudreau
 */
class ModuleInfoCollector implements Coalescer {

    private final Map<Path, JarInfo> infos = new TreeMap<>();
    private final Set<Path> processed = new HashSet<>();
    private final String moduleName;
    private final boolean zeroDates;
    private final ConcatenateMetaInfServices servicesConcat = new ConcatenateMetaInfServices();
    private final Map<String, Coalescer> serviceCoalescers = new HashMap<>();
    boolean written;
    private final boolean open;
    private final boolean checkServiceConstructors;
    private final boolean generateUses;

    ModuleInfoCollector(String name, boolean zeroDates, boolean open, 
            boolean checkServiceConstructors, boolean generateUses) {
        this.moduleName = name;
        this.zeroDates = zeroDates;
        this.open = open;
        this.checkServiceConstructors = checkServiceConstructors;
        this.generateUses = generateUses;
    }

    private JarInfo infoFor(Path path) {
        return infos.computeIfAbsent(path, p -> new JarInfo(p));
    }

    @Override
    public List<String> indexPaths() {
        return Arrays.asList("module-info.class");
    }

    protected boolean read(Path jar, JarEntry entry, JarFile file, InputStream in, MergeLog log) throws Exception {
        if ("module-info.class".equals(entry.getName()) && processed.add(jar)) {
            log.warn("Will coalesce entry {0} in {1}", entry.getName(), jar);
            infoFor(jar).readModuleInfo(entry, file, in, log);
            return true;
        } else if (entry.getName().startsWith("META-INF/services/") && !entry.isDirectory()) {
            noteServiceFile(entry.getName(), file, jar, entry, in, log);
            return true;
        }
        return false;
    }

    protected void write(JarEntry entry, JarOutputStream out, MergeLog log) throws Exception {
        if (zeroDates) {
            entry.setTime(0);
        }
        // Okay, how to do this?  Could create a JFS, generate a fake moduleinfo source and
        // then compile it?
        Path tmp = FileUtils.newTempDir("minfo-synth");
        log.warn("Building synthetic module-info.java in " + tmp);
        log.warn("Writing to {0}", tmp);
        boolean failed = false;
        try {
            Path src = tmp.resolve("src");
            Path classes = tmp.resolve("classes");
            Files.createDirectories(src);
            Files.createDirectories(classes);
            for (Map.Entry<Path, JarInfo> e : infos.entrySet()) {
                try (final JarInputStream in = new JarInputStream(new BufferedInputStream(Files.newInputStream(e.getKey(), StandardOpenOption.READ)))) {
                    JarEntry je = in.getNextJarEntry();
                    while (je != null) {
                        if (je.isDirectory()) {
                            Path dir = classes.resolve(je.getName());
                            if (!Files.exists(dir)) {
                                Files.createDirectories(dir);
                            }
                        } else {
                            Path file = classes.resolve(je.getName());
                            if (!Files.exists(file.getParent())) {
                                Files.createDirectories(file.getParent());
                            }
                            try (final OutputStream outs = new BufferedOutputStream(Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
                                Streams.copy(in, outs);
                            }
                        }
                        je = in.getNextJarEntry();
                    }
                }
            }
            ModuleInfoGenerator gen = new ModuleInfoGenerator(this.infos.values());
            gen.open(open);
            String content = gen.moduleInfo(moduleName, generateUses);
            System.out.println("Generated module-info.java:\n");
            System.out.println(content);
            System.out.println();
            Path moduleInfoSourceFile = src.resolve("module-info.java");
            try (final OutputStream mout = Files.newOutputStream(moduleInfoSourceFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                mout.write(content.getBytes(StandardCharsets.UTF_8));
            }
            DiagLog diagLog = new DiagLog(log);
            log.warn("Begin compile of synthetic module-info.java");
            Set<String> options = CollectionUtils.immutableSetOf("-g");
            Set<String> classNamesForAnnoProcessing = Collections.emptySet();
            JavaCompiler comp = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager mgr = comp.getStandardFileManager(diagLog, Locale.US, StandardCharsets.UTF_8);
            mgr.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(classes.toFile()));
            mgr.setLocation(StandardLocation.SOURCE_PATH, Collections.singleton(src.toFile()));
            Iterable<? extends JavaFileObject> toCompile = mgr.getJavaFileObjects(moduleInfoSourceFile);
            JavaCompiler.CompilationTask task = comp.getTask(diagLog, mgr, diagLog, options, classNamesForAnnoProcessing, toCompile);
            Boolean result = task.call();
            if (!result) {
                failed = true;
                throw new IllegalStateException("Compile failed.  Generated module info is "
                        + "left behind in " + tmp + " for examination");
            }
            log.debug("Copy " + classes.resolve("module-info.class") + " into JAR");
            try (final InputStream is = Files.newInputStream(classes.resolve("module-info.class"), StandardOpenOption.READ)) {
                Streams.copy(is, out);
            }
        } finally {
            written = true;
            if (!failed) {
                FileUtils.deltree(tmp);
            }
        }
    }

    Coalescer wrap(String path, Path inJar, JarEntry entry, MergeLog log) {
        Coalescer del;
        if (serviceCoalescers.containsKey(path)) {
            del = serviceCoalescers.get(path);
            return del;
        } else {
            Coalescer coa = this.servicesConcat.findCoalescer(path, inJar, entry, log);
            if (coa != null) {
                del = new WrappedCoalescer(coa);
                serviceCoalescers.put(path, del);
                return del;
            }
        }
        return null;
    }

    class WrappedCoalescer implements Coalescer {

        private final Coalescer delegate;

        public WrappedCoalescer(Coalescer delegate) {
            this.delegate = delegate;
        }

        @Override
        public String path() {
            return delegate.path();
        }

        @Override
        public void writeCoalesced(JarOutputStream out, MergeLog log) throws Exception {
            delegate.writeCoalesced(out, log);
        }

        @Override
        public void add(Path jar, JarEntry entry, JarFile in, MergeLog log) throws Exception {
            delegate.add(jar, entry, in, log);
            ModuleInfoCollector.this.add(jar, entry, in, log);
        }

        @Override
        public int compareTo(Coalescer o) {
            return delegate.compareTo(o);
        }

        @Override
        public List<String> indexPaths() {
            return delegate.indexPaths();
        }

        @Override
        public String toString() {
            return "Wrap(" + delegate + ")";
        }
    }

    static class DiagLog extends Writer implements DiagnosticListener {

        private final MergeLog log;

        public DiagLog(MergeLog log) {
            this.log = log;
        }

        @Override
        public void report(Diagnostic diagnostic) {
            switch (diagnostic.getKind()) {
                case ERROR:
                    log.error("ModuleInfo Compiler: {0}", diagnostic);
                    break;
                case NOTE:
                    log.debug("ModuleInfo Compiler: {0}", diagnostic);
                    break;
                case MANDATORY_WARNING:
                case OTHER:
                case WARNING:
                    log.log("ModuleInfo Compiler: {0}", diagnostic);
                    break;
            }
            log.log(diagnostic.toString());
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            String s = new String(cbuf, off, len);
            log.log("ModuleInfo Compiler-stdout: " + s);
        }

        @Override
        public void flush() throws IOException {
            // do nothing
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    }

    void notePackage(String path, Path inJar, JarEntry entry, MergeLog log) {
        if (!path.contains("/") || path.startsWith("META-INF")) {
            return;
        }
        log.debug("Note " + UnixPath.get(entry.getName()).toString('.'));
        infoFor(inJar).note(entry);
    }

    @Override
    public String path() {
        return "module-info.class";
    }

    @Override
    public void writeCoalesced(JarOutputStream out, MergeLog log) throws Exception {
        if (!written) {
            written = true;
            //                JarEntry je = new JarEntry(path());
            //                out.putNextEntry(je);
            write(null, out, log);
        }
    }

    @Override
    public void add(Path jar, JarEntry entry, JarFile in, MergeLog log) throws Exception {
        if (entry.getName().equals(path()) || entry.getName().startsWith("META-INF/services/")) {
            try (final InputStream i = in.getInputStream(entry)) {
                boolean success = read(jar, entry, in, i, log);
            }
        }
    }

    void noteServiceFile(String path, JarFile file, Path inJar, JarEntry entry, InputStream in, MergeLog log) throws IOException {
        String service = path.substring("META-INF/services/".length());
        JarInfo info = infoFor(inJar);
        info.readServiceFile(service, file, entry, in, log, 
                checkServiceConstructors);
    }

}
