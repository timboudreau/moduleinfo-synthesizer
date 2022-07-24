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
import java.util.function.Function;
import org.netbeans.modules.classfile.Module;

/**
 *
 * @author Tim Boudreau
 */
class Open extends ModuleEntry<Open> {

    private final Set<String> opensTo = new TreeSet<>();

    Open(Module.OpensEntry oe) {
        super(oe.getPackage().replace('/', '.'));
        opensTo.addAll(oe.getOpensTo());
    }

    Open(String what) {
        super(what);
    }

    @Override
    Open coalesce(Open other) {
        assert target.equals(other.target);
        opensTo.addAll(other.opensTo);
        return this;
    }

    @Override
    void apply(StringBuilder sb, Function<String, String> transformer) {
        sb.append("opens ").append(transformer.apply(target));
        if (!opensTo.isEmpty()) {
            sb.append(" to ");
            for (Iterator<String> it = opensTo.iterator(); it.hasNext();) {
                String t = it.next();
                sb.append(t);
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        sb.append(';');
    }

}
