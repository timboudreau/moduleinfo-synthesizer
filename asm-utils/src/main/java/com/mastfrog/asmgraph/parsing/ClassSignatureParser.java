package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.asm.model.ClassSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.miniparser.MiniParser;
import com.mastfrog.asmgraph.miniparser.Sequence;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author timb
 */
public class ClassSignatureParser implements MiniParser<ClassSignature> {

    @Override
    public ClassSignature parse(Sequence text) {
        Map<String, TypeName> generics = new GenericSignatureParser().parse(text);

        TypeParser supersParser = new TypeParser();
        List<TypeName> supertypes = new ArrayList<>();
        LoopLimiter loops = new LoopLimiter(text, this, supertypes::toString);
        loops.loop2(() -> {
            TypeName supertype = supersParser.parse(text);
            supertypes.add(supertype);
            text.consumeIf('>');
            text.consumeIf(';');
        });

        return new ClassSignature(generics, supertypes);
    }
}
