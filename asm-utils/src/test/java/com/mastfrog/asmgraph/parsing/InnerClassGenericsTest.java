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
package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.ClassProcessor;
import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.miniparser.Sequence;
import static com.mastfrog.asmgraph.parsing.MethodSignatureParserTest.parser;
import static com.mastfrog.asmgraph.parsing.MethodSignatureParserTest.signatures;
import com.mastfrog.asmgraph.record.ClassRecord;
import com.mastfrog.asmgraph.record.DynamicInvocationRecord;
import com.mastfrog.asmgraph.record.FieldAccessRecord;
import com.mastfrog.asmgraph.record.InvocationRecord;
import com.mastfrog.asmgraph.record.MethodRecord;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import static java.util.Collections.unmodifiableMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 * @author timb
 */
public class InnerClassGenericsTest {

    static Map<String, String> signatures;

    @MethodSource
    @ParameterizedTest
    public void testParse(Map.Entry<String, String> sigAndMethodName) {
        String methodName = sigAndMethodName.getValue();
        String signature = sigAndMethodName.getKey();
        Sequence seq = new Sequence(signature);
        MethodSignature sig = parser.parse(seq);

        assertNotNull(sig);
        String isig = sig.toString();
        assertEquals(signature, isig, () -> {
            return methodName + ": Signature mismatch:\n'" + signature
                    + "'\n'" + isig + "'\nSeq:\n" + seq + "\n";
        });
    }

    private static Stream<Map.Entry<String, String>> testParse() {
        return signatures.entrySet().stream();
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        URL loc = MethodSignatureParserTest.class.getProtectionDomain().getCodeSource().getLocation();
        Path path = Paths.get(loc.toURI())
                .resolve("com/mastfrog/asmgraph/asm/sigs/InnerClassGenerics.class");
        if (!Files.exists(path)) {
            throw new AssertionError("No class file at " + path);
        }

        try ( InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
            CP cp = new CP();
            cp.process(in);
            signatures = unmodifiableMap(cp.recordForSignature);
        }
    }

    static class CP implements ClassProcessor {

        Map<String, String> recordForSignature = new TreeMap<>();

        @Override
        public void onMethod(ClassRecord owningType, MethodRecord method, List<? extends InvocationRecord> invocations, List<? extends FieldAccessRecord> fld, List<DynamicInvocationRecord> invokeDynamics, Set<TypeName> referencedTypes) {
            String sig = method.signature;
            if (sig == null) {
                sig = method.descriptor;
                if (sig == null) {
                    return;
                }
            }
            recordForSignature.put(sig, method.name);
        }
    }

}
