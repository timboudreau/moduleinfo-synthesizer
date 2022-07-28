package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.miniparser.Sequence;
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
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.mastfrog.asmgraph.ClassProcessor;
import org.junit.jupiter.api.Test;
import static org.objectweb.asm.Opcodes.ASM9;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Tests that all of the method signatures in sigs/Signatures.java, extracted
 * from its class file, are parsed successfully, and result in a MethodSignature
 * instance whose toString() returns character-for-character identical text.
 *
 * @author timb
 */
public class MethodSignatureParserTest {

    static Map<String, String> signatures;
    static final MethodSignatureParser parser = new MethodSignatureParser();

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

    @Test
    public void testSignatureAsmDoesntLike() {
        String txt = "()Lorg/apache/curator/shaded/com/google/common/collect/AbstractMapBasedMultimap<TK;TV;>.SortedAsMap;Ljava/util/NavigableMap<TK;Ljava/util/Collection<TV;>;>;";
        Sequence seq = new Sequence(txt);
        MethodSignature sig = parser.parse(seq);
        assertEquals(txt, sig.toString(), () -> {
            return "Signature mismatch\n" + txt + "\n" + sig.toString()
                    + "\nin" + seq;
        });
        SignatureReader sr = new SignatureReader(txt);
        sr.accept(new SignatureVisitor(ASM9) {
            @Override
            public void visitEnd() {

            }

        });
    }

    @Test
    public void testInnerTypeNameWithOuterGenerics() {
        String txt = "(Lcom/google/protobuf/SmallSortedMap<TK;TV;>.Entry;)I";
        Sequence seq = new Sequence(txt);
        MethodSignature sig = parser.parse(seq);
        assertEquals(txt, sig.toString(), () -> {
            return "Signature mismatch\n" + txt + "\n" + sig.toString()
                    + "\nin" + seq;
        });
    }

    private static Stream<Map.Entry<String, String>> testParse() {
        return signatures.entrySet().stream();
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        URL loc = MethodSignatureParserTest.class.getProtectionDomain().getCodeSource().getLocation();
        Path path = Paths.get(loc.toURI())
                .resolve("com/mastfrog/asmgraph/asm/sigs/Signatures.class");
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
