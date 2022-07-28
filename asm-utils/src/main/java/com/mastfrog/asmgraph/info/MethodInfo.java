package com.mastfrog.asmgraph.info;

import com.mastfrog.asmgraph.asm.model.Access;
import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import java.util.List;
import java.util.Set;

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
    
}
