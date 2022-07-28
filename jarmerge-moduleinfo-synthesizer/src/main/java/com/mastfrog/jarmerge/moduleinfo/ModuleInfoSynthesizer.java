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
import com.mastfrog.jarmerge.builtin.OmitExcludedPatterns;
import com.mastfrog.jarmerge.builtin.OmitExcludedPrefixes;
import com.mastfrog.jarmerge.builtin.OmitModuleInfo;
import com.mastfrog.jarmerge.spi.Coalescer;
import com.mastfrog.jarmerge.spi.JarFilter;
import com.mastfrog.util.path.UnixPath;
import com.mastfrog.util.service.ServiceProvider;
import java.nio.file.Path;
import static java.util.Collections.emptySet;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(JarFilter.class)
public final class ModuleInfoSynthesizer implements JarFilter<Coalescer> {

    private static final Pattern MODULE_INFO_VERSIONS_PATTERN
            = Pattern.compile("^META-INF/versions/\\d+/module-info\\.class$");
    /**
     * Module name to use.
     */
    public static final String PROP_MODULE_NAME = "moduleName";
    /**
     * Generate an "open" module.
     */
    public static final String PROP_OPEN_MODULE = "openModule";
    /**
     * If set, examine registered service classes for public no-arg
     * constructors, and do not generate provides entries. This is usually not
     * needed, but some Hadoop jars have this pathology.
     */
    public static final String PROP_CHECK_SERVICE_CONSTRUCTORS
            = "checkServiceConstructors";

    public static final String PROP_UNREQUIRE = "unrequire";
    /**
     * Generate <code>uses</code> statements for all services that are provided.
     */
    public static final String PROP_GENERATE_USES
            = "generateUses";
    private final JarMerge merge;
    private boolean zeroDates;
    private ModuleInfoCollector collector;
    // We need these because we may be called ahead of these
    private final OmitExcludedPrefixes prefixSkip;
    private final OmitExcludedPatterns patternSkip;

    public ModuleInfoSynthesizer() {
        this.merge = null;
        prefixSkip = new OmitExcludedPrefixes();
        patternSkip = new OmitExcludedPatterns();
    }

    private ModuleInfoSynthesizer(JarMerge merge) {
        this.merge = merge;
        prefixSkip = new OmitExcludedPrefixes(merge);
        patternSkip = new OmitExcludedPatterns(merge);
    }

    @Override
    public JarFilter<Coalescer> configureInstance(JarMerge jarMerge) {
        return new ModuleInfoSynthesizer(jarMerge);
    }

    @Override
    public int precedence() {
        return -10000000;
    }

    @Override
    public boolean enabledByDefault() {
        return true;
    }

    public boolean generatedUses() {
        if (merge == null) {
            return false;
        }
        return "true".equals(merge.extensionProperties.get(PROP_GENERATE_USES));
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

    boolean checkServiceConstructors() {
        if (merge == null) {
            return false;
        }
        String val = merge.extensionProperties.getOrDefault(
                PROP_CHECK_SERVICE_CONSTRUCTORS, "false");
        return "true".equals(val);
    }

    @Override
    public boolean omit(String path, Path inJar, MergeLog log) {
        // THe skippers may be set to omit module-info.class, but we must
        // not or we will never get a chance to process it.
        if (path.contains("module-info.class")) {
            return false;
        }
        // These are provided by java.xml in the JDK and will ALWAYS result in
        // an unusable module jar
        if (path.startsWith("javax/xml")) {
            if (this.collector != null) {
                this.collector.addRequire("java.xml");
            }
            return true;
        }
//        return "module-info.class".equals(path);
        boolean result = patternSkip.omit(path, inJar, log) || prefixSkip.omit(path, inJar, log);
        if (result) {
            System.out.println("OMIT " + path + " from " + inJar.getFileName());
        }
        return result;
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
        if (sb.length() == 0) {
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
            collector = new ModuleInfoCollector(moduleName(), zeroDates, open(),
                    checkServiceConstructors(), generatedUses());
            collector.unrequire(unrequired());
        }
        return collector;
    }

    Set<String> unrequired() {
        if (merge == null) {
            return emptySet();
        }
        String val = merge.extensionProperties.get(PROP_UNREQUIRE);
        if (val != null && !val.isBlank()) {
            Set<String> result = new HashSet<>();
            for (String s : val.split(",")) {
                result.add(s.trim());
            }
            return result;
        }
        return emptySet();
    }

    static boolean isModuleInfo(String path) {
        // Note, this is not great.  If there is META-INF/9/module-info.class
        // and META-INF/11/module-info.class, we are going to coalesce ONE
        // of these and ignore all of the others, based on JAR order
        return "module-info.class".equals(path)
                || MODULE_INFO_VERSIONS_PATTERN.matcher(path).find();
    }

    @Override
    public synchronized Coalescer coalescer(String path, Path inJar, JarEntry entry, MergeLog log) {
        // These are provided by java.xml in the JDK and will ALWAYS result in
        // an unusable module jar
        if (path.startsWith("javax/xml")) {
            return null;
        }
        if (omit(path, inJar, log)) {
            System.out.println("SKIP " + path + " " + inJar.getFileName());
            return null;
        }
        if (isModuleInfo(entry.getName())) {
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
        return "Reads the bytecode of all found module-info.class files and "
                + "synthesizes a merged version";
    }

    @Override
    public boolean supersedes(JarFilter<?> other) {
        return other.as(OmitModuleInfo.class) != null
                || other.as(ConcatenateMetaInfServices.class) != null;
    }

    @Override
    public void setZeroDates(boolean val) {
        zeroDates = val;
    }
}
