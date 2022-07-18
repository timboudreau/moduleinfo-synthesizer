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

import com.mastfrog.jarmerge.JarMerge;
import com.mastfrog.jarmerge.MergeLog;
import com.mastfrog.jarmerge.builtin.ConcatenateMetaInfServices;
import com.mastfrog.jarmerge.builtin.OmitModuleInfo;
import com.mastfrog.jarmerge.spi.Coalescer;
import com.mastfrog.jarmerge.spi.JarFilter;
import com.mastfrog.util.path.UnixPath;
import com.mastfrog.util.service.ServiceProvider;
import java.nio.file.Path;
import java.util.jar.JarEntry;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(JarFilter.class)
public final class ModuleInfoSynthesizer implements JarFilter<Coalescer> {

    public static final String PROP_MODULE_NAME = "moduleName";
    public static final String PROP_OPEN_MODULE = "openModule";
    private final JarMerge merge;
    private boolean zeroDates;
    private ModuleInfoCollector collector;

    public ModuleInfoSynthesizer() {
        this.merge = null;
    }

    private ModuleInfoSynthesizer(JarMerge merge) {
        this.merge = merge;
    }

    @Override
    public JarFilter<Coalescer> configureInstance(JarMerge jarMerge) {
        return new ModuleInfoSynthesizer(jarMerge);
    }

    @Override
    public int precedence() {
        return 1000;
    }

    @Override
    public boolean enabledByDefault() {
        return true;
    }

    @Override
    public boolean isCritical() {
        return true;
    }

    boolean open() {
        if (merge == null) {
            return true;
        }
        String val = merge.extensionProperties.get(PROP_OPEN_MODULE);
        return val == null ? true : "true".equals(val.trim());
    }

    @Override
    public boolean omit(String path, Path inJar, MergeLog log) {
//        return "module-info.class".equals(path);
        return false;
    }

    private static String moduleNameFromJarName(String jarName) {
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 3);
        }
        StringBuilder sb = new StringBuilder();
        for (String part : jarName.split("[ .,_-]")) {
            if (part.isEmpty()) {
                continue;
            }
            StringBuilder sub = new StringBuilder();
            for (int i = 0; i < part.length(); i++) {
                if (i == 0) {
                    if (!Character.isJavaIdentifierStart(part.charAt(0))) {
                        if (part.length() > 1) {
                            sub.append('x');
                        }
                        if (Character.isJavaIdentifierPart(part.charAt(0))) {
                            sub.append(part.charAt(0));
                        }
                    }
                } else {
                    if (Character.isJavaIdentifierPart(part.charAt(0))) {
                        sub.append(part.charAt(i));
                    }
                }
            }
            if (sb.length() > 0) {
                sb.append('.');
                sb.append(sub);
            }
        }
        if (sb.isEmpty()) {
            sb.append("module.name.not.derived");
        }
        return sb.toString();
    }

    private String moduleName() {
        if (merge != null) {
            return merge.extensionProperties.getOrDefault(PROP_MODULE_NAME, moduleNameFromJarName(merge.jarName));
        }
        return "module.name.not.passed";
    }

    private synchronized ModuleInfoCollector collector() {
        if (collector == null) {
            collector = new ModuleInfoCollector(moduleName(), zeroDates, open());
        }
        return collector;
    }

    @Override
    public synchronized Coalescer coalescer(String path, Path inJar, JarEntry entry, MergeLog log) {
        if ("module-info.class".equals(entry.getName())) {
            return collector();
        } else {
            if (path.startsWith("META-INF/services")) {
                Coalescer wrapped = collector().wrap(path, inJar, entry, log);
                return wrapped;
            } else if (!path.startsWith("META-INF") && path.endsWith(".class") && isPossibleJavaPackage(path)) {
                collector().notePackage(path, inJar, entry, log);
            }
        }
        return null;
    }

    private static boolean isPossibleJavaPackage(String path) {
        UnixPath parentPath = UnixPath.get(path).getParent();
        if (parentPath == null) {
            return false;
        }
        for (int i = 0; i < parentPath.getNameCount(); i++) {
            String name = parentPath.getName(i).toString();
            for (int j = 0; j < name.length(); j++) {
                char c = name.charAt(j);
                switch (j) {
                    case 0:
                        if (!Character.isJavaIdentifierStart(c)) {
                            return false;
                        }
                        break;
                    default:
                        if (!Character.isJavaIdentifierPart(c)) {
                            return false;
                        }
                }
            }
        }
        return true;
    }

    @Override
    public String name() {
        return "module-info-merge";
    }

    @Override
    public String description() {
        return "Reads the bytecode of all found module-info.class files and syntesizes a merged version";
    }

    @Override
    public boolean supersedes(JarFilter<?> other) {
        return other.as(OmitModuleInfo.class) != null || other.as(ConcatenateMetaInfServices.class) != null;
    }

    @Override
    public void setZeroDates(boolean val) {
        zeroDates = val;
    }
}
