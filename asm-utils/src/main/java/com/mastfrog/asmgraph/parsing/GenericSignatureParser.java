package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.miniparser.CharPredicate;
import com.mastfrog.asmgraph.miniparser.DelimiterPair;
import com.mastfrog.asmgraph.miniparser.MiniParser;
import com.mastfrog.asmgraph.miniparser.Sequence;
import com.mastfrog.function.state.Obj;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author timb
 */
class GenericSignatureParser implements MiniParser<Map<String, TypeName>> {

    private static final CharPredicate STOP_ON_PARENS = CharPredicate.of('(');

    @Override
    public Map<String, TypeName> parse(Sequence text) {
        if (text.position() == 0) {
            Obj<Map<String, TypeName>> result = Obj.of(Collections.emptyMap());
            boolean success = text.tryConsume('<', () -> {
                int positionOfClose = text.delimiterScan(DelimiterPair.ANGLES, STOP_ON_PARENS);
                if (positionOfClose < 0) {
                    return false;
                }
                text.limited(positionOfClose, () -> {
                    GenericTypePairsParser gtpp = new GenericTypePairsParser();
                    result.set(gtpp.parse(text));
                    return true;
                });
                return true;
            });
            if (success) {
                return result.get();
            }
        }
        return Collections.emptyMap();
    }

}
