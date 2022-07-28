package com.mastfrog.asmgraph.info;

import com.mastfrog.asmgraph.asm.model.Access;
import com.mastfrog.asmgraph.asm.model.TypeName;
import java.util.Set;

/**
 *
 * @author timb
 */
public interface FieldInfo {

    Set<Access> access();

    String name();

    TypeName type();

    TypeName rawType();

    Object defaultValue();
    
}
