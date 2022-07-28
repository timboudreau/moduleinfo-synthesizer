package com.mastfrog.asmgraph.record;

import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.record.DynamicInvocationRecord.TargetKind;

/**
 * Describes a single invokeDynamic instruction in a method.
 *
 * @author Tim Boudreau
 */
public interface DynamicInvocationInfo {

    TypeName coercedTo();

    MethodSignature functionCoercion();

    String functionName();

    boolean isInterface();

    MethodSignature lambdaFunctionSignature();

    String lambdaName();

    TypeName lambdaOwner();

    MethodSignature otherSignature();

    TargetKind targetKind();

}
