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
package com.mastfrog.test.test.reloc.verification;

import java.net.URL;
import java.io.InputStream;
import java.util.jar.*;
import java.util.zip.*;
// This will show up as an error in any ide that looks to the
// classes directory of the neighboring project - but it is correct - this
// is the name the class is remapped to, and we are testing that that
// worked.
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import mug.wurbles.Reloctest;

public class TestRelocVerificationTest {

    @Test
    public void testMain() throws Exception {
        // The main method contains all the assertions we need
        Reloctest.main(new String[0]);
    }

    @Test
    public void inspectJar() throws Exception {
        Class<?> c = Reloctest.class;
        URL u = c.getProtectionDomain().getCodeSource().getLocation();
        System.out.println("U is " + u);
        if (u.toString().endsWith("jar")) {
            java.io.File file = new java.io.File(u.toURI());
            try (JarFile jf = new JarFile(file)) {
                Manifest man = jf.getManifest();
                Attributes mainAttrs = man.getMainAttributes();
                String iv = mainAttrs.getValue("Implementation-Vendor");
                assertTrue(iv.startsWith("smart-jar-merge"), "Imp vendor: '" + iv + "'");
                
                String amn = mainAttrs.getValue("Automatic-Module-Name");
                assertEquals("foo.test.thing", amn);
                
                String main = mainAttrs.getValue("Main-Class");
                assertEquals(c.getName(), main);
                
                assertNotNull(man);
                
                ZipEntry en = jf.getEntry("META-INF/services/urble.libs.SomeInterface");
                assertNotNull(en, "No entry for META-INF/services/urble.libs.SomeInterface");
                
                try (InputStream in = jf.getInputStream(en)) {
                    String text = new String(in.readAllBytes(), "UTF-8");
                    assertEquals("mug.wurbles.Reloctest\n", text);
                }
                
                assertNull(jf.getEntry("com/mastfrog/reloclib/RStrings.class"));
                assertNull(jf.getEntry("com/mastfrog/reloctest/Reloctest.class"));
                assertNotNull(jf.getEntry("notremapped/NotRemapped.class"));
            }
        }
    }
}
