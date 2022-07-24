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

import com.mastfrog.util.path.UnixPath;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a single class file in a single JAR file which needs to be
 * rewritten.
 *
 * @author Tim Boudreau
 */
final class RelocationEntry {

    private final String newPackage;
    private final String oldPackage;
    private final String path;
    private final Path jar;

    public RelocationEntry(String newPackage, String path, Path jar) {
        this.newPackage = newPackage;
        this.path = path;
        this.jar = jar;
        UnixPath up = UnixPath.get(path);
        if (up.getNameCount() == 1) {
            oldPackage = "";
        } else {
            oldPackage = up.getParent().toString('.');
        }
    }

    public String newJarEntryName() {
        UnixPath name = UnixPath.get(path).getFileName();
        if (newPackage.isEmpty()) {
            return name.toString();
        }
        return UnixPath.get(newPackage.replace('.', '/'))
                .resolve(name).toString();
    }

    String path() {
        return path;
    }

    String oldPackage() {
        return oldPackage;
    }

    String newPackage() {
        return newPackage;
    }

    Path in() {
        return jar;
    }

    @Override
    public String toString() {
        return oldPackage + " -> " + newPackage + " for "
                + UnixPath.get(path).getFileName() + " in " + jar.getFileName()
                + " --> " + newJarEntryName();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.newPackage);
        hash = 97 * hash + Objects.hashCode(this.oldPackage);
        hash = 97 * hash + Objects.hashCode(this.path);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RelocationEntry other = (RelocationEntry) obj;
        if (!Objects.equals(this.newPackage, other.newPackage)) {
            return false;
        }
        if (!Objects.equals(this.oldPackage, other.oldPackage)) {
            return false;
        }
        return Objects.equals(this.path, other.path);
    }
}
