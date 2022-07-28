package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.miniparser.CharPredicate;
import com.mastfrog.asmgraph.miniparser.MiniParser;
import com.mastfrog.asmgraph.miniparser.Sequence;
import java.util.ArrayList;
import static java.util.Collections.emptyList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author timb
 */
public class MethodSignatureParser implements MiniParser<MethodSignature> {

    @Override
    public MethodSignature parse(Sequence text) {

        Map<String, TypeName> params = null;
        List<TypeName> args = null;
        if (text.curr() == '<') {
            GenericTypePairsParser gtpp = new GenericTypePairsParser();
            params = gtpp.parse(text);
            text.consumeIf('>');
        }
        if (text.curr() == '(' && text.la(1) != ')') {
            MethodArgumentsParser p = new MethodArgumentsParser();
            args = p.parse(text);
        } else if (text.curr() == '(' && text.la(1) == ')') {
            text.consume(2);
        }
        TypeParser tp = new TypeParser();
        TypeName ret = tp.parse(text);
        return new MethodSignature(params, args, ret);
    }

    static class MethodArgumentsParser implements MiniParser<List<TypeName>> {

        @Override
        public List<TypeName> parse(Sequence text) {
            text.consumeIf('(');
            if (text.consumeIf(')')) {
                return emptyList();
            }
            List<TypeName> result = new ArrayList<>(16);
            TypeParser tp = new TypeParser();
            int end = text.delimiterScan('(', ')', CharPredicate.FALSE);
            if (end > text.position()) {
                text.limited(end, () -> {
                    LoopLimiter limiter = new LoopLimiter(text, this, result::toString);
                    limiter.loop2(() -> {
                        TypeName t = tp.parse(text);
                        text.consumeIf(';');
                        result.add(t);
                    });
                    return !result.isEmpty();
                });
            }
            text.consumeIf(')');
            return result;
        }
    }
}
