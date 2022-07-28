package com.mastfrog.asmgraph.record;

import com.mastfrog.asmgraph.info.FieldInfo;
import com.mastfrog.asmgraph.Parsing;
import com.mastfrog.asmgraph.asm.model.Access;
import com.mastfrog.asmgraph.asm.model.TypeName;
import java.util.Set;

/**
 * Simply encapsulates the arguments ASM passes to a visitor on encountering a
 * class, and implements ClassInfo to provide access to parsed representations
 * of those arguments.
 *
 * @author Tim Boudreau
 */
public final class FieldRecord implements FieldInfo {
    
    public final int access;
    public final String name;
    public final String descriptor;
    public final String signature;
    public final Object value;

    public FieldRecord(int access, String name, String descriptor, String signature, Object value) {
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.value = value;
    }
    
    @Override
    public Set<Access> access() {
        return Access.from(access);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TypeName type() {
        if (signature == null) {
            return rawType();
        }
        return Parsing.fieldSignature(signature);
    }

    @Override
    public TypeName rawType() {
        return Parsing.fieldSignature(descriptor);
    }

    @Override
    public Object defaultValue() {
        return value;
    }

    @Override
    public String toString() {
        return Access.stringFrom(access) + " " + type().sourceName() + " " + name() + ";";
    }
}
