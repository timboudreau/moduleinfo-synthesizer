package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.parsing.ClassSignatureParser;
import com.mastfrog.asmgraph.asm.model.ClassSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.miniparser.Sequence;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author timb
 */
public class ClassSignatureParserTest {

    @Test
    public void testParse() {
        String txt = "<S:Ljava/lang/Number;:Ljava/lang/CharSequence;:Ljava/lang/Iterable<Ljava/lang/String;>;>"
                + "Lcom/mastfrog/asmgraph/asm/sigs/Thing<Ljava/lang/String;Lcom/mastfrog/asmgraph/asm/sigs/Arglet;Ljava/lang/IllegalThreadStateException;>;"
                + "Lcom/mastfrog/asmgraph/asm/sigs/IfaceOne;"
                + "Lcom/mastfrog/asmgraph/asm/sigs/IfaceTwo<Ljava/lang/Short;>;";

        Sequence text = new Sequence(txt);
        ClassSignatureParser instance = new ClassSignatureParser();
        ClassSignature result = instance.parse(text);
        assertEquals(txt, result.toString(), () -> {
            return "Signature mismatch:\n" + txt + "\n" + result + "\nSequence:\n" + text;
        });
    }

    @Test
    public void testParseNoGenerics() {
        String txt = "Ljava/lang/ThreadLocal<Ljava/lang/ref/SoftReference<Lcom/ctc/wstx/io/BufferRecycler;>;>;";
        Sequence text = new Sequence(txt);
        ClassSignatureParser instance = new ClassSignatureParser();
        ClassSignature result = instance.parse(text);
        assertEquals(txt, result.toString(), () -> {
            return "Signature mismatch:\n" + txt + "\n" + result + "\nSequence:\n" + text;
        });

    }
    
    @Test
    public void testConversion() {
        String txt = "Ljava/lang/Object;Lorg/apache/hadoop/fs/RemoteIterator<Lorg/apache/hadoop/fs/LocatedFileStatus;>;";
        Sequence seq = new Sequence(txt);
        ClassSignatureParser tp = new ClassSignatureParser();
        ClassSignature res = tp.parse(seq);
        assertEquals(txt, res.toString(), () -> {
            return txt + "\n" + res+ "\nin\n" + seq;
        });
        
        ClassSignature nue = res.transform(t -> t.replaceAll("hadoop", "wookies").replaceAll("java/", "urbles/"));
        System.out.println("NUE: " + nue);
        String exp2 =  "Lurbles/lang/Object;Lorg/apache/wookies/fs/RemoteIterator<Lorg/apache/wookies/fs/LocatedFileStatus;>;";
        assertEquals(exp2, nue.toString());
    }
    
}
