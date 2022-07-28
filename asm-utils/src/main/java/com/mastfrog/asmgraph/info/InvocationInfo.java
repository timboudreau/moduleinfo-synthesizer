package com.mastfrog.asmgraph.info;

import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;

/**
 *
 * @author timb
 */
public interface InvocationInfo {

    MethodSignature invoked();

    boolean isInterfaceMethod();

    String name();

    int opcode();

    TypeName owner();
    
}
