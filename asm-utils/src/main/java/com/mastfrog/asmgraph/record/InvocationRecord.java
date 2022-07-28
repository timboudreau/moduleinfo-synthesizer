package com.mastfrog.asmgraph.record;

import com.mastfrog.asmgraph.info.InvocationInfo;
import static com.mastfrog.asmgraph.Parsing.methodSignature;
import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;

/**
 * Simply encapsulates the arguments ASM passes to a visitor on encountering a
 * class, and implements ClassInfo to provide access to parsed representations
 * of those arguments.
 *
 * @author Tim Boudreau
 */
public final class InvocationRecord implements InvocationInfo {
    
    public final int opcode;
    public final String owner;
    public final String name;
    public final String descriptor;
    public final boolean isInterface;

    public InvocationRecord(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        this.opcode = opcode;
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
        this.isInterface = isInterface;
    }

    @Override
    public String toString() {
        return "inv name='" + name + "' on '" + owner + "'"
                + (descriptor != null && !descriptor.isBlank() ? " desc='" + descriptor + "'" : "'") 
                + (isInterface ? " IFACE" : "");
    }
    
    @Override
    public boolean isInterfaceMethod() {
        return isInterface;
    }
    
    @Override
    public int opcode() {
        return opcode;
    }
    
    @Override
    public MethodSignature invoked() {
        return methodSignature(descriptor);
    }
    
    @Override
    public TypeName owner() {
        return TypeName.simpleName(owner);
    }
    
    @Override
    public String name() {
        return name;
    }
}
