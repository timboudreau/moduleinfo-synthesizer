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

import static com.mastfrog.jarmerge.moduleinfo.ModuleEntry.canonicalize;
import com.mastfrog.jarmerge.spi.ClassNameRewriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.classfile.Module;

/**
 *
 * @author Tim Boudreau
 */
final class JarsData {

    final Map<String, ExpEntry> exports = new HashMap<>();
    final Map<String, Open> opens = new HashMap<>();
    final Map<String, Req> requires = new HashMap<>();
    final Map<String, Prov> provides = new HashMap<>();
    final Set<String> uses = new TreeSet<>();
    final Set<String> coalescedModuleNames = new TreeSet<>();
    final boolean open;
    private final boolean generateUseEntryForProvides;
    private final Set<String> automaticModulesMerged;
    private final Set<String> syntheticRequires;

    JarsData(boolean open, boolean generateUseEntryForProvides, Set<String> automaticModulesMerged, Set<String> syntheticRequires) {
        this.open = open;
        this.generateUseEntryForProvides = generateUseEntryForProvides;
        this.automaticModulesMerged = automaticModulesMerged;
        this.syntheticRequires = syntheticRequires;
    }

    void write(StringBuilder into) {
        Set<String> usesWritten = new HashSet<>();
        Set<String> providesWritten = new HashSet<>();
        Set<String> writtenRequires = new HashSet<>();
        ClassNameRewriter rew = ClassNameRewriter.get();
        String lineHead = "\n    ";
        requires.forEach((name, req) -> {
            if (coalescedModuleNames.contains(req.target()) || automaticModulesMerged.contains(req.target())) {
                return;
            }
            into.append(lineHead);
            req.apply(into, rew);
            writtenRequires.add(req.target());
        });
        // We detect attempts to bundle javax/xml packages and replace them
        // with a synthetic requiring in of java.xml.
        for (String synth : syntheticRequires) {
            if (writtenRequires.add(synth)) {
                into.append(lineHead).append("require ").append(synth).append(';');
            }
        }
        into.append('\n');
        exports.forEach((name, req) -> {
            into.append(lineHead);
            req.apply(into, rew);
        });
        if (!open) {
            into.append('\n');
            opens.forEach((name, req) -> {
                into.append(lineHead);
                req.apply(into, rew);
            });
        }
        into.append('\n');
        provides.forEach((name, req) -> {
            if (providesWritten.add(name)) {
                into.append(lineHead);
                req.apply(into, rew);
            }
            String u = canonicalize(name);
            if (generateUseEntryForProvides && !usesWritten.contains(u)) {
                usesWritten.add(u);
                into.append(lineHead).append("uses ").append(u).append(';');
            }
        });
        into.append('\n');
        for (String u : uses) {
            u = canonicalize(u);
            if (!usesWritten.contains(u)) {
                usesWritten.add(u);
                into.append(lineHead);
                into.append("uses ").append(u).append(';');
            }
        }
        if (!open) {
            List<Open> synthetic = new ArrayList<>();
            exports.forEach((name, req) -> {
                if (!opens.containsKey(name)) {
                    synthetic.add(req.toOpen());
                }
            });
            Collections.sort(synthetic);
            if (!synthetic.isEmpty()) {
                into.append("\n\n");
                into.append("    // synthetic entries\n");
                for (Open op : synthetic) {
                    into.append(lineHead);
                    op.apply(into, rew);
                }
            }
        }
    }

    private <E extends ModuleEntry<E>> void addEntry(E nue, Map<String, E> map) {
        map.compute(nue.target(), (t, old) -> {
            if (old == null) {
                return nue;
            }
            return old.coalesce(nue);
        });
    }

    void nonModuleExport(String pkg) {
        addEntry(new ExpEntry(pkg), exports);
    }

    void encounterExport(org.netbeans.modules.classfile.Module.ExportsEntry ee) {
        ExpEntry nue = new ExpEntry(ee);
        addEntry(nue, exports);
    }

    void encounterModule(String moduleName, String ver) {
        coalescedModuleNames.add(moduleName);
    }

    void encounterOpen(Module.OpensEntry op) {
        addEntry(new Open(op), opens);
    }

    void encounterRequires(Module.RequiresEntry req) {
        addEntry(new Req(req), requires);
    }

    void encounterProvides(String type, String impl) {
        Prov p = new Prov(type, impl);
        addEntry(p, provides);
    }

    void encounterProvides(Module.ProvidesEntry prov) {
        Prov p = new Prov(prov);
        addEntry(p, provides);
    }

    void encounterUse(String externalName) {
        uses.add(externalName);
    }

}
