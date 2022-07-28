package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.miniparser.CharPredicate;
import com.mastfrog.asmgraph.miniparser.MiniParser;
import com.mastfrog.asmgraph.miniparser.Sequence;
import com.mastfrog.function.state.Obj;

/**
 * Parser for the generic <i>signature</i> of a method, as opposed to generics
 * on an argument type. These have a different syntax than argument generics,
 * since they support compound types and explicitly indicate interface types.
 *
 * @author Tim Boudreau
 */
class GenericSignatureTypeNameParser implements MiniParser<String> {

    static final CharPredicate STOP = CharPredicate.of(';', '>', '(', ')');

    @Override
    public String parse(Sequence text) {
        text.consumeIf('<');
        Obj<String> result = Obj.create();
        boolean found = text.provisionally(() -> {
            String name = text.scanTo(':', STOP);
            if (name == null) {
                return false;
            }
            result.set(name);
            return true;
        });
        return result.get();
    }

}
