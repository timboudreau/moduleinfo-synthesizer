/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.asm.model.Access;
import com.mastfrog.asmgraph.record.ClassRecord;
import com.mastfrog.asmgraph.record.FieldRecord;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.mastfrog.asmgraph.ClassProcessor;

/**
 *
 * @author timb
 */
public class FieldSignatureTest {

    static List<FieldRecord> fields;

    @MethodSource
    @ParameterizedTest
    public void testSignatures(FieldRecord rec) throws Exception {
        System.out.println(Access.stringFrom(rec.access) + " " + rec.type().sourceName() + " " + rec.name() + ";");
        String sig = rec.signature == null ? rec.descriptor : rec.signature;
        assertEquals(sig, rec.type().internalName());
    }

    private static Stream<FieldRecord> testSignatures() {
        return fields.stream();
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        URL loc = MethodSignatureParserTest.class.getProtectionDomain().getCodeSource().getLocation();
        Path path = Paths.get(loc.toURI())
                .resolve("com/mastfrog/asmgraph/asm/sigs/Fields.class");
        if (!Files.exists(path)) {
            throw new AssertionError("No class file at " + path);
        }
        List<FieldRecord> fields = new ArrayList<>();
        try ( InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {

            ClassProcessor l = new ClassProcessor() {
                @Override
                public void onFields(ClassRecord clazz, List<? extends FieldRecord> flds) {
                    fields.addAll(flds);
                }
            };

            l.process(in);
        }
        assertFalse(fields.isEmpty(), "No fields found");
        FieldSignatureTest.fields = fields;
    }

}
