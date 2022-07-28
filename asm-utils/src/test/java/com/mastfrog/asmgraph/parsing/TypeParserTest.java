package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.asm.model.ArrayTypeName;
import com.mastfrog.asmgraph.asm.model.PrimitiveTypes;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.miniparser.Sequence;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.mastfrog.asmgraph.asm.model.TypeKind.PARAMETERIZED_OBJECT_TYPE;

/**
 *
 * @author timb
 */
public class TypeParserTest {
    
    @Test
    public void test() {
        String txt = "Lfoo/a/InCGen<TK;TV;>.En.Sub.SubV<TR;>.SubSubValue.SubSubSubValue";
        Sequence seq = new Sequence(txt);
        TypeParser tp = new TypeParser();
        TypeName tn = tp.parse(seq);

        String got = tn.internalName();
        assertEquals(txt, got, () -> {
            StringBuilder sb = new StringBuilder(txt + "\n" + got + "\n" + seq);
            tn.visitTypeNames(nm -> {
                sb.append("\n  * ").append(nm.kind()).append(' ')
                        .append(nm.internalName()).append(' ')
                        .append(nm.getClass().getSimpleName());
            });
            return sb.toString();
        });
    }

    @Test
    public void testParsePrimitiveTypes() {
        for (PrimitiveTypes t : PrimitiveTypes.values()) {
            TypeName name = t.typeName();
            TypeParser tp = new TypeParser();
            Sequence seq = new Sequence(name.internalName());
            TypeName parsed = tp.parse(seq);
            assertEquals(name, parsed);
            assertSame(name, parsed);
            ArrayTypeName atn = new ArrayTypeName(name);
            String arr = atn.internalName();
            TypeName arrParsed = tp.parse(new Sequence(arr));
            assertEquals(arrParsed, atn);
            assertEquals(t.name().toLowerCase() + "[]", arrParsed.sourceName());
        }
    }

    @Test
    public void testManyDimensions() {
        String txt = "[[[[Ljava/lang/String;";
        Sequence seq = new Sequence(txt);
        TypeParser tp = new TypeParser();
        TypeName tn = tp.parse(seq);

        String got = tn.internalName();
        assertEquals(txt, got, () -> {
            StringBuilder sb = new StringBuilder(txt + "\n" + got + "\n" + seq);
            tn.visitTypeNames(nm -> {
                sb.append("\n  * ").append(nm.kind()).append(' ')
                        .append(nm.internalName()).append(' ')
                        .append(nm.getClass().getSimpleName());
            });
            return sb.toString();
        });
    }

    @Test
    public void testIntersectionType() {
        String txt = "Ljava/lang/String;:Ljava/lang/CharSequence;:Ljava/lang/Iterable<+Ljava/lang/CharSequence;>;:Ljava/util/function/Consumer<-TB;>;:Ljava/lang/Comparable<Ljava/lang/String;>;";
        Sequence text = new Sequence(txt);
        TypeParser tp = new TypeParser();

        TypeName res = tp.parse(text);

        assertEquals(txt, res.internalName(), () -> {
            return txt + "\n" + res.internalName() + "\n" + text;
        });
    }

    @Test
    public void testThreeSequentialWildcards() {
        String txt = "Lcom/mastfrog/asmgraph/asm/sigs/Triple<***>;";
        Sequence seq = new Sequence(txt);
        TypeParser tp = new TypeParser();
        TypeName nm = tp.parse(seq);

        assertEquals(txt, nm.internalName());
    }

    @Test
    public void testFourSequentialWildcards() {
        String txt = "Lcom/mastfrog/function/QuadConsumer<****>;";
        Sequence seq = new Sequence(txt);
        TypeParser tp = new TypeParser();
        TypeName nm = tp.parse(seq);

        assertEquals(txt, nm.internalName());
    }

    @Test
    public void testFiveSequentialWildcards() {
        String txt = "Lcom/mastfrog/function/PentaConsumer<*****>;";
        Sequence seq = new Sequence(txt);
        TypeParser tp = new TypeParser();
        TypeName nm = tp.parse(seq);

        assertEquals(txt, nm.internalName());
    }

    @Test
    public void testSixSequentialWildcards() {
        String txt = "Lcom/mastfrog/function/SextaConsumer<******>;";
        Sequence seq = new Sequence(txt);
        TypeParser tp = new TypeParser();
        TypeName nm = tp.parse(seq);

        assertEquals(txt, nm.internalName());
    }

    @Test
    public void testParseSingleTypeParamater() {
        Sequence seq = new Sequence("Ljava/lang/Iterable<Ljava/lang/String;>;");

        TypeParser tp = new TypeParser();
        TypeName result = tp.parse(seq);

        assertNotNull(result);
        assertSame(PARAMETERIZED_OBJECT_TYPE, result.kind());

        assertEquals("Ljava/lang/Iterable<Ljava/lang/String;>;", result.internalName());
    }

    @Test
    public void testParseTwoTypeParameter() {
        Sequence seq = new Sequence("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Number;>;");

        TypeParser tp = new TypeParser();
        TypeName result = tp.parse(seq);

        assertEquals("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Number;>;", result.internalName());
    }

    @Test
    public void testParseTwoTypeParameterCapture() {
        Sequence seq = new Sequence("Ljava/util/Map<-Ljava/lang/String;+Ljava/lang/Number;>;");

        TypeParser tp = new TypeParser();
        TypeName result = tp.parse(seq);

        assertEquals("Ljava/util/Map<-Ljava/lang/String;+Ljava/lang/Number;>;", result.internalName());

        assertEquals("java.util.Map<? super java.lang.String, ? extends java.lang.Number>", result.sourceName());
    }

    @Test
    public void testComplexSig1() {
        Sequence seq = new Sequence("<K::Lcom/mastfrog/signatures/Key;:Ljava/lang/CharSequence;:Ljava/lang/Iterable<Ljava/lang/Character;>;V:TS;M::Ljava/util/Map<-TK;+TV;>;>");
        GenericTypePairsParser pairs = new GenericTypePairsParser();

        Map<String, TypeName> result = pairs.parse(seq);
        assertTrue(result.containsKey("K"));
        assertTrue(result.containsKey("V"));
        assertTrue(result.containsKey("M"));
    }
    
    @Test
    public void testConversion() {
        String txt = "Lorg/apache/hadoop/fs/RemoteIterator<Lorg/apache/hadoop/fs/LocatedFileStatus;>;";
        Sequence seq = new Sequence(txt);
        TypeParser tp = new TypeParser();
        TypeName res = tp.parse(seq);
        assertEquals(txt, res.internalName(), () -> {
            return txt + "\n" + res.internalName() + "\nin\n" + seq;
        });
        TypeName nue = res.transform(t -> t.replaceAll("hadoop", "wookies").replaceAll("org/", "urbles/"));
        System.out.println("NUE: " + nue);
        String exp2 =  "Lurbles/apache/wookies/fs/RemoteIterator<Lurbles/apache/wookies/fs/LocatedFileStatus;>;";
        assertEquals(exp2, nue.internalName());
    }
}
