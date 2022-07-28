package com.mastfrog.asmgraph.miniparser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author timb
 */
public class SequenceTest {

    @Test
    public void testBasicDelimScan() {
        CharPredicate stop = CharPredicate.of('(');
        Sequence seq = new Sequence("<foo,bar,baz>()");
        seq.consume();
        int ix = seq.delimiterScan('<', '>', stop);
        assertTrue(ix > 0);
    }

    @Test
    public void testDelimScanWithNesting() {
        CharPredicate stop = CharPredicate.of('(');
        Sequence seq = new Sequence("<K::java/util/Map<Ljava/lang/String;Ljava/lang/Iterable<Ljava/lang/Number;>;>;>;()");

        assertEquals('<', seq.curr());
        seq.consume();
        assertEquals('K', seq.curr());
        assertEquals(1, seq.position());

        int end = seq.delimiterScan(DelimiterPair.ANGLES, stop, DelimiterPair.ANGLES);
        assertTrue(end > 0, "Wrong end " + end);
        seq.limited(end, () -> {
            String limS = seq.contents();
            assertEquals("K::java/util/Map<Ljava/lang/String;Ljava/lang/Iterable<Ljava/lang/Number;>;>;", limS);

            int nextStart = seq.positionOf('<');

            seq.consumeTo(nextStart + 1);

            int next = seq.delimiterScan(DelimiterPair.ANGLES, stop, DelimiterPair.ANGLES);
            assertTrue(next > 0, "Should be able to find nested");

            seq.limited(next, () -> {
                assertEquals("Ljava/lang/String;Ljava/lang/Iterable<Ljava/lang/Number;>;", seq.contents());
                return true;
            });

            return true;
        });

    }

}
