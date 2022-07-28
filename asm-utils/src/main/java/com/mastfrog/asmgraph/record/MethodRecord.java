package com.mastfrog.asmgraph.record;

import com.mastfrog.asmgraph.info.MethodInfo;
import com.mastfrog.asmgraph.Parsing;
import com.mastfrog.asmgraph.asm.model.Access;
import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Simply encapsulates the arguments ASM passes to a visitor on encountering a
 * class, and implements ClassInfo to provide access to parsed representations
 * of those arguments.
 *
 * @author Tim Boudreau
 */
public final class MethodRecord implements MethodInfo {

    public final int access;
    public final String name;
    public final String descriptor;
    public final String signature;
    public final String[] exceptions;

    public MethodRecord(int access, String name, String descriptor,
            String signature, String[] exceptions) {
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.exceptions = exceptions;
    }

    public Set<Access> access() {
        return Access.from(access);
    }

    @Override
    public String toString() {
        return "mth " + Access.stringFrom(access) + " "
                + name
                + " " + signature().toCode();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public MethodSignature signature() {
        if (signature == null) {
            return descriptor();
        }
        return Parsing.methodSignature(signature);
    }

    @Override
    public MethodSignature descriptor() {
        return Parsing.methodSignature(descriptor);
    }

    @Override
    public List<TypeName> exceptions() {
        if (exceptions == null || exceptions.length == 0) {
            return Collections.emptyList();
        }
        List<TypeName> result = new ArrayList<>(exceptions.length);
        for (String ex : exceptions) {
            TypeName tn = TypeName.simpleName(ex);
            result.add(tn);
        }
        return result;
    }

}
