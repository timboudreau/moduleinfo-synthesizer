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

import java.util.function.Function;
import org.netbeans.modules.classfile.Access;
import org.netbeans.modules.classfile.Module;

/**
 * A requires entry discovered in a module-info.class or otherwise synthesized.
 *
 * @author Tim Boudreau
 */
final class Req extends ModuleEntry<Req> {

    private int flags;
    private String ver;

    Req(Module.RequiresEntry r) {
        super(r.getModule());
        ver = r.getVersion();
    }

    public String toString() {
        return target() + " " + ver + " " + flags + " @ " + System.identityHashCode(this);
    }

    public String target() {
        if (ver != null) {
            String c = "@" + ver;
            if (target.endsWith(c)) {
                return target.substring(0, target.length() - c.length());
            }
        } else if (target.contains("@")) {
            int ix = target.indexOf('@');
            if (ix > 0) {
                return target.substring(0, ix);
            }
        }
        return target;
    }

    @Override
    Req coalesce(Req other) {
        assert target.equals(other.target);
        flags |= other.flags;
        if (ver == null) {
            ver = other.ver;
        }
        return this;
    }

    @Override
    void apply(StringBuilder sb, Function<String, String> ignored) {
        sb.append("requires ");
        if ((flags & Access.TRANSITIVE) != 0) {
            sb.append("transitive ");
        }
        if ((flags & Access.STATIC_PHASE) != 0) {
            sb.append("static ");
        }
        if ((flags & Access.SYNTHETIC) != 0) {
            sb.append("synthetic ");
        }
        if ((flags & Access.MANDATED) != 0) {
            sb.append("mandated ");
        }
        sb.append(target());
        if (ver != null) {
            //                sb.append("@")
            //                        .append(ver);
        }
        sb.append(';');
        sb.append(" // ").append(this);
    }

}
