package com.mastfrog.asmgraph.asm.model;

import java.util.Set;

/**
 *
 * @author timb
 */
public interface AccessControlled {
    
    public int rawAccess();
    
    default Set<Access> access() {
        return Access.from(rawAccess());
    }
    
    default String accessString() {
        return Access.stringFrom(rawAccess());
    }
}
