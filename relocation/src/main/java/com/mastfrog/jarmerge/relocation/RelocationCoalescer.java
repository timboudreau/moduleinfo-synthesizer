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

import com.mastfrog.jarmerge.MergeLog;
import com.mastfrog.jarmerge.spi.Coalescer;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 *
 * @author Tim Boudreau
 */
final class RelocationCoalescer implements Coalescer {

    private final RelocationEntry entry;
    private final RelocationCoalescers owner;
    private byte[] bytes;

    RelocationCoalescer(RelocationEntry entry, RelocationCoalescers owner) {
        this.entry = entry;
        this.owner = owner;
    }

    public String originalPath() {
        return entry.path();
    }

    @Override
    public String path() {
        if ("module-info.class".equals(originalPath())) {
            return originalPath();
        }
        return entry.newJarEntryName();
    }

    @Override
    public void writeCoalesced(JarOutputStream out, MergeLog log) throws Exception {
        if (bytes == null) {
            throw new IllegalStateException("Bytes never read for " + entry);
        }
        owner.writeCoalesced(out, log, entry,
                () -> new ByteArrayInputStream(bytes));
    }

    @Override
    public void add(Path jar, JarEntry entry, JarFile in, MergeLog log) throws Exception {
        try ( InputStream input = in.getInputStream(entry)) {
            bytes = input.readAllBytes();
        }
    }
}
