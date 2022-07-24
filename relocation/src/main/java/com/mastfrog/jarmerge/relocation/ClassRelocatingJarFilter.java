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

import com.mastfrog.jarmerge.JarMerge;
import com.mastfrog.jarmerge.MergeLog;
import com.mastfrog.jarmerge.spi.ClassNameRewriter;
import com.mastfrog.jarmerge.spi.Coalescer;
import com.mastfrog.jarmerge.spi.JarFilter;
import com.mastfrog.util.path.UnixPath;
import com.mastfrog.util.service.ServiceProvider;
import java.nio.file.Path;
import java.util.jar.JarEntry;

/**
 * Implements simple package relocation, with special-casing for the default
 * package. Add this library as a dependency to maven-merge-dependencies, and
 * then specify entries like
 * <code>&ltpackage:com.needs.rename&gt;org.renamed.to&lt;/package:com.needs.rename&gt;</code>
 * or
 * <code>&lt;defaultPackageDest&gt;was.default.package&lt;/defaultPackageDest&gt;</code>.
 *
 * @author Tim Boudreau
 */
@ServiceProvider(JarFilter.class)
public final class ClassRelocatingJarFilter implements JarFilter<Coalescer> {

    public static final String DEFAULT_PACKAGE_COALESCENCE = "defaultPackageDest";
    private static final String REPACKAGE_PREFIX = "package:";
    private final JarMerge settings;
    private final ClassNameRewriterImpl rewriterImpl = new ClassNameRewriterImpl();
    private boolean logged;
    private boolean zeroDates;
    private RelocationCoalescers coas;
    private final boolean isUnused;

    public ClassRelocatingJarFilter() {
        settings = null;
        isUnused = false;
    }

    public ClassRelocatingJarFilter(JarMerge settings) {
        this.settings = settings;
        boolean unused = !settings.extensionProperties.containsKey(DEFAULT_PACKAGE_COALESCENCE);
        if (unused) {
            for (String k : settings.extensionProperties.keySet()) {
                if (k.startsWith(REPACKAGE_PREFIX)) {
                    unused = false;
                    break;
                }
            }
        }
        isUnused = unused;
    }

    String replacementPackage(String pkg) {
        if (pkg == null) {
            return null;
        }
        String setting = REPACKAGE_PREFIX + pkg;
        String replacement = settings == null ? null : settings.extensionProperties.get(setting);
        if (replacement == null && pkg.indexOf('/') > 0) {
            setting = REPACKAGE_PREFIX + pkg.replace('/', '.');
            replacement = settings == null ? null : settings.extensionProperties.get(setting);
        }
        return replacement == null ? pkg : replacement;
    }

    public String transformPackage(String path) {
        UnixPath up = UnixPath.get(path);
        if ("class".equals(up.extension())) {
            if (up.getNameCount() == 1) {
                String dp = settings == null ? null : settings.extensionProperties.get(DEFAULT_PACKAGE_COALESCENCE);
                if (dp == null) {
                    return null;
                }
                return dp;
            } else {
                String setting = REPACKAGE_PREFIX + up.getParent().toString('.');
                String pkg = settings == null ? null : settings.extensionProperties.get(setting);
                return pkg;
            }
        }
        return null;
    }

    @Override
    public Coalescer coalescer(String path, Path inJar, JarEntry entry, MergeLog log) {
        if (isUnused) {
            if (!logged) {
                logged = true;
                log.warn("jarmerge-relocation is on classpath but no packages are configured to relocate - will not run.");
            }
            return null;
        }
        if (!entry.isDirectory() && path.endsWith(".class")) {
            String newPackage = transformPackage(path);
            Coalescer result;
            if (newPackage != null) {
                result = coa().add(path, newPackage, entry, inJar);
            } else {
                result = coa().addPath(path, entry, inJar);
            }
            return result;
        }
        return null;
    }

    private synchronized RelocationCoalescers coa() {
        if (coas == null) {
            coas = new RelocationCoalescers(this);
        }
        return coas;
    }

    @Override
    public ClassRelocatingJarFilter configureInstance(JarMerge jarMerge) {
        return new ClassRelocatingJarFilter(jarMerge);
    }

    @Override
    public int precedence() {
        return 10;
    }

    @Override
    public boolean enabledByDefault() {
        return !isUnused;
    }

    @Override
    public String description() {
        return "Can move classes to different packages";
    }

    @Override
    public void setZeroDates(boolean val) {
        this.zeroDates = val;
    }

    boolean isZeroDates() {
        return zeroDates;
    }

    @Override
    public boolean omit(String path, Path inJar, MergeLog log) {
        return false;
    }

    @Override
    public String name() {
        return "relocate-classes";
    }

    @Override
    public <T> T as(Class<T> type) {
        if (type.isInstance(rewriterImpl)) {
            return type.cast(rewriterImpl);
        }
        return JarFilter.super.as(type);
    }

    class ClassNameRewriterImpl implements ClassNameRewriter {

        @Override
        public String apply(String t) {
            if (t == null) {
                return null;
            }
            if (coas != null) {
                String arg = t;
                String result = coas.remap(arg);
                if (result.equals(arg)) {
                    arg = arg.replace('.', '/');
                    result = coas.remap(arg);
                    if (!result.equals(arg)) {
                        return result.replace('/', '.');
                    } else {
                        return t;
                    }
                } else {
                    return result;
                }
            }
            int dotIx = t.lastIndexOf('.');
            if (dotIx > 0) {
                String rpkg = replacementPackage(t);
                if (rpkg.equals(t)) {
                    String stripped = t.substring(0, dotIx);
                    rpkg = replacementPackage(stripped);
                    if (!rpkg.equals(stripped)) {
                        return rpkg + '.' + t.substring(dotIx);
                    }
                }
            }
            return t;
        }
    }
}
