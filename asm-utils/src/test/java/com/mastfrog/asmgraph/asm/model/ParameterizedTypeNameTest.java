package com.mastfrog.asmgraph.asm.model;

import static java.util.Collections.singletonList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author timb
 */
public class ParameterizedTypeNameTest {

    /**
     * Test of kind method, of class ParameterizedTypeName.
     */
    @Test
    public void testKind() {
        PrefixedTypeName pt = new PrefixedTypeName(TypePrefix.OBJECT, RawTypeName.of("java/lang/Iterable"));

        assertEquals("Ljava/lang/Iterable;", pt.internalName());
        assertEquals("Ljava/lang/Iterable", pt.rawName());
        assertEquals("java.lang.Iterable", pt.sourceName());

        PrefixedTypeName string = new PrefixedTypeName(TypePrefix.OBJECT, RawTypeName.of("java/lang/String"));
        assertEquals("Ljava/lang/String;", string.internalName());
        assertEquals("Ljava/lang/String", string.rawName());
        assertEquals("java.lang.String", string.sourceName());

        ParameterizedTypeName par = new ParameterizedTypeName(pt, singletonList(string));

        assertEquals("java.lang.Iterable<java.lang.String>", par.sourceName());
        assertEquals("Ljava/lang/Iterable<Ljava/lang/String;>", par.rawName());
        assertEquals("Ljava/lang/Iterable<Ljava/lang/String;>;", par.internalName());
    }

}
