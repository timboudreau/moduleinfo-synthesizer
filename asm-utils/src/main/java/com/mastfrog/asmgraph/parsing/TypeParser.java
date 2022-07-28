/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.asm.model.ArrayTypeName;
import com.mastfrog.asmgraph.asm.model.CaptureTypeName;
import com.mastfrog.asmgraph.asm.model.Captures;
import com.mastfrog.asmgraph.asm.model.CombinedTypeName;
import com.mastfrog.asmgraph.asm.model.InnerClassGenericsTypeName;
import com.mastfrog.asmgraph.asm.model.InterfaceType;
import com.mastfrog.asmgraph.asm.model.ParameterizedTypeName;
import com.mastfrog.asmgraph.asm.model.PrefixedTypeName;
import com.mastfrog.asmgraph.asm.model.PrimitiveTypes;
import com.mastfrog.asmgraph.asm.model.RawTypeName;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.asm.model.TypeParameterTypeName;
import com.mastfrog.asmgraph.asm.model.TypePrefix;
import com.mastfrog.asmgraph.asm.model.WildcardTypeName;
import com.mastfrog.asmgraph.miniparser.CharPredicate;
import com.mastfrog.asmgraph.miniparser.MiniParser;
import com.mastfrog.asmgraph.miniparser.Sequence;
import com.mastfrog.function.state.Obj;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author timb
 */
public final class TypeParser implements MiniParser<TypeName> {

    @Override
    public TypeName parse(Sequence text) {
        char curr = text.curr();
        switch (curr) {
            case ':':
                text.consume();
                return InterfaceType.of(parse(text));
            case 'T':
                text.consume();
                TypeNameParser genericNameParser = new TypeNameParser();
                String realGeneric = genericNameParser.parse(text);
                return new TypeParameterTypeName(realGeneric);
            case '*':
                text.consume();
                return WildcardTypeName.WILDCARD;
            case '-':
                text.consume();
                return new CaptureTypeName(Captures.SUPER, parse(text));
            case '+':
                text.consume();
                return new CaptureTypeName(Captures.EXTENDS, parse(text));
            case '[':
                text.consume();
                return new ArrayTypeName(parse(text));
        }
        TypePrefix prefix = TypePrefix.matching(text.curr());
        TypeName result = null;
        if (prefix == null) {
            PrimitiveTypes pt = PrimitiveTypes.match(text.curr());
            if (pt != null) {
                result = pt.typeName();
                text.consume();
            }
        } else {
            text.consume();
            if (!prefix.requiresObjectType()) {
                PrimitiveTypes pt = PrimitiveTypes.match(text.curr());
                if (pt != null) {
                    text.consume();
                    result = new PrefixedTypeName(prefix, pt.typeName());
                }
            }
            if (result == null) {
                switch (prefix) {
                    case ARRAY:
                        result = new PrefixedTypeName(prefix, parse(text));
                        break;
                    case OBJECT:
                        result = new PrefixedTypeName(prefix, RawTypeName.of(new TypeNameParser().parse(text)));
                        break;
                    case CAPTURE_OF:
                    case INTERSECTION:
                        throw new UnsupportedOperationException();
                }
            }
        }
        text.consumeIf(';');
        if (result == null && text.curr() == ':') {
            // We are in a type list
            List<TypeName> combo = new ArrayList<>();
            LoopLimiter limiter = new LoopLimiter(text, this, combo::toString);
            while (text.curr() == ':') {
                text.consume();
                TypeName next = parse(text);
                combo.add(next);
                text.consumeIf(';');
                limiter.onLoop();
            }
            if (!combo.isEmpty()) {
                result = CombinedTypeName.from(combo);
            }
        } else if (text.curr() == ':') {
            List<TypeName> combo = new ArrayList<>();
            combo.add(result);
            LoopLimiter limiter = new LoopLimiter(text, this, combo::toString);
            while (text.curr() == ':') {
                text.consume();
                TypeName next = InterfaceType.of(parse(text));
                combo.add(next);
                text.consumeIf(';');
                limiter.onLoop();
            }
            result = CombinedTypeName.from(combo);
        }
        if (result != null && text.curr() == '<') {
            text.consume();
            GenericsParser gp = new GenericsParser();
            List<TypeName> args = gp.parse(text);
            if (!args.isEmpty()) {
                result = new ParameterizedTypeName(result, args);
            }
        }

        if (result != null && (text.curr() == '.' || text.prev() == '.')) {
            if (text.prev() == '.') {
                text.backup();
            }
            result = appendInnerGenerics(this, result, text);
        }
        if (result == null) {
            throw new IllegalStateException("Did not find a type name in " + text);
        }
        return result;
    }

    static TypeName appendInnerGenerics(MiniParser<?> p, TypeName result, Sequence text) {
        Obj<TypeName> res = Obj.of(result);
        int ix = text.positionOf(')');
        if (ix > 0) {
            text.limited(ix, () -> {
                TypeName tn = _appendInnerGenerics(p, result, text, res);
                res.set(tn);
                return tn != result;
            });
        } else {
            _appendInnerGenerics(p, result, text, res);
        }
        return res.get();
    }

    private static TypeName _appendInnerGenerics(MiniParser<?> p, TypeName result, Sequence text, Obj<TypeName> res) {
        // This is not pretty - principally because GenericsParser moves the cursor
        // one character past the trailing dot, which is not what we want.

        LoopLimiter loops = new LoopLimiter(text, p, res::toString);
        loops.loop2(() -> {
            text.consumeIf('.');
            String item = text.scanTo('.', CharPredicate.of('<', '>', '(', ')', ';'));
            if (item == null) {
                item = text.scanTo('<', CharPredicate.of('>', '(', ')', ';'));
                if (text.prev() == '<') {
                    text.backup();
                }
            }
            if (item == null && !text.isDone()) {
                TypeName nue = applyGenerics(res.get(), text);
                if (nue != res.get()) {
                    res.set(nue);
                    if (text.prev() == '.') {
                        text.backup();
                    }
                    return;
                }
                item = text.consumeRemainder();
            }
            if (item == null) {
                loops.breakLoop();
                return;
            }
            res.set(new InnerClassGenericsTypeName(res.get(), item));
            res.set(applyGenerics(res.get(), text));
        });
        return res.get();
    }

    static TypeName applyGenerics(TypeName result, Sequence text) {
        if (text.consumeIf('<')) {
            GenericsParser gp = new GenericsParser();
            List<TypeName> args = gp.parse(text);
            if (!args.isEmpty()) {
                return new ParameterizedTypeName(result, args);
            }
        }
        return result;

    }

}
