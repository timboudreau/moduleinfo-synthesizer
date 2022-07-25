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
package com.mastfrog.test.combined;

import static com.mastfrog.test.combined.TestCombinedModularLib.allServices;
import com.mastfrog.test.modular.service.lib.ModularService;
import java.util.*;

/**
 *
 * @author timb
 */
public class TestCombinedModularLib {

    public static void main(String[] args) {
        System.out.println("ALL SERVICES");
        for (String s : serviceNames()) {
            System.out.println("  * " + s);
        }
    }

    static List<ModularService> allServices() {
        List<ModularService> services = new ArrayList<>();
        for (ModularService svc : java.util.ServiceLoader.load(ModularService.class)) {
            services.add(svc);
        }
        Module module = TestCombinedModularLib.class.getModule();
        if (module != null) {
            System.out.println("My module is " + module.getName());
            ModuleLayer layer = module.getLayer();
            if (layer != null) {
                for (ModularService svc : java.util.ServiceLoader.load(layer, ModularService.class)) {
                    services.add(svc);
                }
            } else {
                System.out.println("MODULE HAS NO LAYER");
            }
        } else {
            System.out.println("NO MODULE");
        }

        return services;
    }

    static List<String> serviceNames() {
        List<String> svcs = new ArrayList<>();
        allServices().forEach(svc -> svcs.add(svc.name()));
        return svcs;
    }
}
