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
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Module;

/**
 * A provides entry for a given service class, either discovered in a
 * module-info.class, or in a META-INF/services file, or synthesized based on an
 * inference from discovered data.
 *
 * @author Tim Boudreau
 */
class Prov extends ModuleEntry<Prov> {

    private final Set<String> implementations = new TreeSet<>();

    Prov(Module.ProvidesEntry p) {
        super(canonicalize(p.getService().toString()));
        for (ClassName cn : p.getImplementations()) {
            implementations.add(cn.getExternalName());
        }
    }

    Prov(String type, String impl) {
        super(canonicalize(type));
        this.implementations.add(canonicalize(impl));
    }

    @Override
    Prov coalesce(Prov other) {
        assert target.equals(other.target);
        implementations.addAll(other.implementations);
        return this;
    }

    @Override
    void apply(StringBuilder sb, Function<String, String> transformer) {
        sb.append("provides ").append(transformer.apply(target)).append(" with ");
        for (Iterator<String> it = implementations.iterator(); it.hasNext();) {
            sb.append("\n         ");
            String i = it.next();
            sb.append(transformer.apply(canonicalize(i)));
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(';');
    }

}
