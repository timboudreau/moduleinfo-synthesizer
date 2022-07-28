package com.mastfrog.asmgraph.info;

import com.mastfrog.asmgraph.asm.model.TypeName;

/**
 *
 * @author timb
 */
public interface FieldAccessInfo {

    String name();
    
    TypeName owner();

    TypeName fieldType();

    int opcode();
    
}
