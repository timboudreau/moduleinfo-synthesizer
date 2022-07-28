package com.mastfrog.asmgraph.record;

import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.info.FieldAccessInfo;

/**
 *
 *
 * @author Tim Boudreau
 */
public final class FieldAccessRecord implements FieldAccessInfo {

    public final int opcode;
    public final String owner;
    public final String name;
    public final String descriptor;

    public FieldAccessRecord(int opcode, String owner, String name, 
            String descriptor) {
        this.opcode = opcode;
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public String toString() {
        return name + " of='" + owner + "'"
                + (descriptor != null
                && !descriptor.isBlank() ? " desc='"
                + descriptor + "'" : "'");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TypeName fieldType() {
        return TypeName.simpleName(descriptor);
    }

    @Override
    public int opcode() {
        return opcode;
    }

    @Override
    public TypeName owner() {
        return TypeName.simpleName(owner);
    }

}
