package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.asm.model.TypeName;
import static com.mastfrog.asmgraph.asm.model.WildcardTypeName.WILDCARD;
import com.mastfrog.asmgraph.miniparser.CharPredicate;
import com.mastfrog.asmgraph.miniparser.MiniParser;
import com.mastfrog.asmgraph.miniparser.Sequence;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author timb
 */
class GenericsParser implements MiniParser<List<TypeName>> {

    private static final CharPredicate STOP = CharPredicate.of('(');

    @Override
    public List<TypeName> parse(Sequence text) {
        String input = text.toString();
        text.consumeIf('<');
        List<TypeName> names = new ArrayList<>();
        boolean found = text.provisionally(() -> {
            int lim = text.delimiterScan('<', '>', STOP);
            if (lim >= 0) {
                return text.limited(lim + 1, () -> {
                    LoopLimiter limiter = new LoopLimiter(text, this, names::toString);
                    NonSignatureGenericParser tp = new NonSignatureGenericParser();
                    limiter.loop2(() -> {
                        if (text.curr() == '*') {
                            names.add(WILDCARD);
                            text.consume();
                            return;
                        }
                        String prev = text.toString();
                        TypeName item = tp.parse(text);
                        if (item == null) {
                            limiter.breakLoop();
                            return;
                        }
                        if (item == null) {
                            throw new IllegalStateException("No type found.\n" + prev
                                    + " have " + names + "\nInput was\n" + input);
                        }
                        names.add(item);
                        text.consumeIf('>');
                    });
                    return !names.isEmpty();
                });
            }
            return false;
        });
        return names;
    }

}
