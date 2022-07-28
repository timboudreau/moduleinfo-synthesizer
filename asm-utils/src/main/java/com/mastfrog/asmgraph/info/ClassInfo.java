package com.mastfrog.asmgraph.info;

import com.mastfrog.asmgraph.asm.model.Access;
import com.mastfrog.asmgraph.asm.model.ClassSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import java.util.List;
import java.util.Set;

/**
 *
 * @author timb
 */
public interface ClassInfo {

    int version();

    Set<Access> access();

    TypeName name();

    TypeName supertypeName();

    List<TypeName> interfaces();

    ClassSignature signature();
    
}
