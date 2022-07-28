package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.miniparser.MiniParser;
import com.mastfrog.asmgraph.miniparser.Sequence;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses name/value pairs of generic name, generic type.
 *
 * @author timb
 */
class GenericTypePairsParser implements MiniParser<Map<String, TypeName>> {

    @Override
    public Map<String, TypeName> parse(Sequence text) {
        GenericSignatureTypeNameParser names = new GenericSignatureTypeNameParser();
        TypeParser types = new TypeParser();
        Map<String, TypeName> result = new LinkedHashMap<>();
        LoopLimiter limiter = new LoopLimiter(text, this, result::toString);
        limiter.loop(() -> {
            String name = names.parse(text);
            if (name != null) {
                System.out.flush();
                TypeName type = types.parse(text);
                if (type != null) {
                    result.put(name, type);
                } else {
                    throw new IllegalStateException("No type for '" + name + "' in\n" + text
                            + "\ncurr at " + text.curr());
                }
                text.consumeIf(';');
            } else {
                if (result.isEmpty()) {
                    throw new IllegalStateException("No name in\n" + text);
                }
                limiter.breakLoop();
            }
        });
        return result;
    }

}
