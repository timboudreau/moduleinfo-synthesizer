package com.mastfrog.asmgraph.info;

import com.mastfrog.asmgraph.asm.model.Access;
import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 *
 * @author timb
 */
public interface MethodInfo {

    Set<Access> access();

    String name();

    MethodSignature signature();

    MethodSignature descriptor();

    List<TypeName> exceptions();

    default String toCodeString(TypeName owner) {
        return toCodeString(owner, TypeNameStyle.SOURCE);
    }

    default String toCodeString(TypeName owner, TypeNameTransformer f) {
        return toCodeString(owner, f, TypeNameTransformer.argumentNameSynthesizer());
    }

    default String toCodeString(TypeName owner, TypeNameTransformer typeNames,
            Function<TypeName, String> synth) {
        String name = name();
        String space = typeNames.isMachineFormat() ? "" : " ";
        String dot = typeNames.isMachineFormat() ? "" : ".";

        MethodSignature sig = !typeNames.includesGenerics() ? descriptor() : signature();
        StringBuilder gens = new StringBuilder();
        if (typeNames.includesGenerics()) {
            if (!sig.typeParameters().isEmpty()) {
                gens.append(" <");
                for (Iterator<Map.Entry<String, TypeName>> it
                        = sig.typeParameters().entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, TypeName> e = it.next();
                    gens.append(e.getKey());
                    String genTypeName = typeNames.apply(e.getValue());
                    if (typeNames.isComplete() || (!"Object".equals(genTypeName) && !"java.lang.Object".equals(genTypeName))) {
                        gens.append(':');
                        gens.append(genTypeName);
                    }
                    if (!typeNames.isMachineFormat() && it.hasNext()) {
                        gens.append(", ");
                    }
                }
                gens.append(">");
            }
        }

        StringBuilder args = new StringBuilder().append("(");
        for (Iterator<TypeName> it = sig.arguments().iterator(); it.hasNext();) {
            TypeName t = it.next();
            args.append(typeNames.apply(t));
            if (!typeNames.isMachineFormat()) {
                args.append(' ').append(synth.apply(t));
                if (it.hasNext()) {
                    args.append(", ");
                }
            }
        }
        args.append(')');

        StringBuilder exceptions = new StringBuilder();
        if (!typeNames.isMachineFormat()) {
            List<TypeName> excs = exceptions();
            if (!excs.isEmpty()) {
                if (!typeNames.isMachineFormat()) {
                    exceptions.append(" throws ");
                } else {
                    exceptions.append(' ');
                }
                for (Iterator<TypeName> it = excs.iterator(); it.hasNext();) {
                    TypeName ex = it.next();
                    exceptions.append(typeNames.apply(ex));
                    if (it.hasNext()) {
                        exceptions.append(", ");
                    }
                }
            }
        }

        return Access.stringFrom(access())
                + gens
                + space + typeNames.apply(sig.returnType()) + space
                + typeNames.apply(owner) + dot
                + name + args + exceptions;
    }

}
