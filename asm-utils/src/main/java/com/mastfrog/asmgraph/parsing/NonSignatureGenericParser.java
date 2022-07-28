/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.asm.model.ArrayTypeName;
import com.mastfrog.asmgraph.asm.model.CaptureTypeName;
import com.mastfrog.asmgraph.asm.model.Captures;
import com.mastfrog.asmgraph.asm.model.ParameterizedTypeName;
import com.mastfrog.asmgraph.asm.model.PrefixedTypeName;
import com.mastfrog.asmgraph.asm.model.PrimitiveTypes;
import com.mastfrog.asmgraph.asm.model.RawTypeName;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.asm.model.TypeParameterTypeName;
import com.mastfrog.asmgraph.asm.model.TypePrefix;
import com.mastfrog.asmgraph.asm.model.WildcardTypeName;
import com.mastfrog.asmgraph.miniparser.MiniParser;
import com.mastfrog.asmgraph.miniparser.Sequence;
import static com.mastfrog.asmgraph.parsing.TypeParser.appendInnerGenerics;
import java.util.List;

/**
 *
 * @author timb
 */
class NonSignatureGenericParser implements MiniParser<TypeName> {

    @Override
    public TypeName parse(Sequence text) {
        char c = text.curr();
        PrimitiveTypes prim = PrimitiveTypes.match(c);
        if (prim != null) {
            text.consume();
            return prim.typeName();
        }
        TypeName result = null;
        switch (c) {
            case '[':
                text.consume();
                return new ArrayTypeName(parse(text));
            case '*':
                text.consume();
                return WildcardTypeName.WILDCARD;
            case '-':
                text.consume();
                result = new CaptureTypeName(Captures.SUPER, parse(text));
                break;
            case '+':
                text.consume();
                result = new CaptureTypeName(Captures.EXTENDS, parse(text));
                break;
            case 'T':
                text.consume();
                TypeNameParser genericNames = new TypeNameParser();
                String nm = genericNames.parse(text);
                switch (text.curr()) {
                    case ';':
                        text.consume();
                        break;
                }
                return new TypeParameterTypeName(nm);
            case 'L':
                text.consume();
                TypeNameParser tnp = new TypeNameParser();
                String name = tnp.parse(text);
                if (name == null) {
                    throw new IllegalStateException("Did not find a name in\n" + text);
                }
                result = new PrefixedTypeName(TypePrefix.OBJECT, RawTypeName.of(name));
                switch (text.curr()) {
                    case ';':
                        text.consume();
                        break;
                    case '<':
                        GenericsParser generics = new GenericsParser();
                        List<TypeName> found = generics.parse(text);
                        if (!found.isEmpty()) {
                            result = new ParameterizedTypeName(result, found);
                        }
                }
                break;
        }
        if (result != null && (text.curr() == '.' || text.prev() == '.')) {
            if (text.prev() == '.') {
                text.backup();
            }
            result = appendInnerGenerics(this, result, text);
        }
        return result;
    }
}
