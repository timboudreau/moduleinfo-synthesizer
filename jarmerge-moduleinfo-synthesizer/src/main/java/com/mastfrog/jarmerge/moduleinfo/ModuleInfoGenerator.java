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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Tim Boudreau
 */
class ModuleInfoGenerator {

    private final List<JarInfo> info;
    private boolean open = true;
    private final Set<String> automaticModulesMerged;

    public ModuleInfoGenerator(Collection<? extends JarInfo> info, Set<String> automaticModulesMerged) {
        this.info = new ArrayList<>(info);
        Collections.sort(this.info);
        this.automaticModulesMerged = automaticModulesMerged;
    }

    ModuleInfoGenerator open() {
        return open(true);
    }

    ModuleInfoGenerator open(boolean val) {
        this.open = val;
        return this;
    }

    public String moduleInfo(String moduleName, boolean generateUses) {
        JarsData jd = new JarsData(open, generateUses, automaticModulesMerged);
        for (JarInfo ji : info) {
            ji.collect(jd);
        }
        StringBuilder sb = new StringBuilder();
        if (open) {
            sb.append("open module ");
        } else {
            sb.append("module ");
        }
        sb.append(moduleName).append(" {\n");
        jd.write(sb);
        sb.append("\n}").toString();
        return sb.toString();
    }
}
