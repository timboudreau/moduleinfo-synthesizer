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

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Tim Boudreau
 */
class ExpEntry extends ModuleEntry<ExpEntry> {

    private int flags;
    private final Set<String> to = new TreeSet<>();

    public ExpEntry(org.netbeans.modules.classfile.Module.ExportsEntry e) {
        super(e.getPackage().replace('/', '.'));
        to.addAll(e.getExportsTo());
    }

    ExpEntry(String packageName) {
        super(packageName);
    }

    public Open toOpen() {
        return new Open(target());
    }

    @Override
    void apply(StringBuilder sb) {
        if ("java.base".equals(target())) {
            return;
        }
        sb.append("exports ").append(target());
        if (!to.isEmpty()) {
            sb.append(" to");
            for (Iterator<String> it = to.iterator(); it.hasNext();) {
                String s = it.next();
                sb.append("\n        ");
                sb.append(s);
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
        }
        sb.append(";");
    }

    @Override
    ExpEntry coalesce(ExpEntry other) {
        assert other.target.equals(target);
        flags |= other.flags;
        to.addAll(other.to);
        return this;
    }

}
