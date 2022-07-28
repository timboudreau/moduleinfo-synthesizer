package com.mastfrog.asmgraph.record;

import com.mastfrog.asmgraph.Parsing;
import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.H_GETFIELD;
import static org.objectweb.asm.Opcodes.H_GETSTATIC;
import static org.objectweb.asm.Opcodes.H_INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.H_INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.H_NEWINVOKESPECIAL;
import static org.objectweb.asm.Opcodes.H_PUTFIELD;
import static org.objectweb.asm.Opcodes.H_PUTSTATIC;

/**
 *
 * @author timb
 */
public class DynamicInvocationRecord implements DynamicInvocationInfo {

    public final String lambdaFunctionMethodName;
    public final String methodCoercion;
    public final String methodCoercionTargetSignature;
    public final String otherSignature;
    public final String lambdaName;
    public final String lambdaOwner;
    public final int tag;
    private final boolean isInterface;

    public DynamicInvocationRecord(String lambdaFunctionMethodName, String methodCoercion, String methodCoercionTargetSignature, String otherSignature, String lambdaName, String lambdaOwner, int tag, boolean isInterface) {
        this.lambdaFunctionMethodName = lambdaFunctionMethodName;
        this.methodCoercion = methodCoercion;
        this.methodCoercionTargetSignature = methodCoercionTargetSignature;
        this.otherSignature = otherSignature;
        this.lambdaName = lambdaName;
        this.lambdaOwner = lambdaOwner;
        this.tag = tag;
        this.isInterface = isInterface;
    }

    @Override
    public String toString() {
        return targetKind() + " " + lambdaName + " in " + lambdaOwner + " as "
                + coercedTo() + " sig " + methodCoercionTargetSignature
                + (isInterface ? " iface" : "");
    }

    @Override
    public boolean isInterface() {
        return isInterface;
    }

    @Override
    public String lambdaName() {
        return lambdaName;
    }

    @Override
    public TypeName lambdaOwner() {
        return TypeName.simpleName(lambdaOwner);
    }

    @Override
    public TargetKind targetKind() {
        return TargetKind.targetKind(tag);
    }

    @Override
    public String functionName() {
        return lambdaFunctionMethodName;
    }

    @Override
    public TypeName coercedTo() {
        return functionCoercion().returnType();
    }

    @Override
    public MethodSignature functionCoercion() {
        return Parsing.methodSignature(methodCoercion);
    }

    @Override
    public MethodSignature lambdaFunctionSignature() {
        return Parsing.methodSignature(methodCoercionTargetSignature);
    }

    @Override
    public MethodSignature otherSignature() {
        // XXX what IS this - arg 2 of bootstrapMethodArguments
        return Parsing.methodSignature(otherSignature);
    }

    /*
    
average2 InvD 
    accept // accept is the aliased lambda function method - DoubleConsumer.accept()

    // Now, what the heck is this signature?
    // Nothing actually takes a Dbl and returns a DoubleConsumer, so it is
    // some kind of coercion
    (Lcom/mastfrog/function/state/Dbl;)Ljava/util/function/DoubleConsumer;
    
    The method signature of what we are being coerced into
 TYPE1 (D)V // what we are being coerced into (what forEach accepts)
 TYPE2 (D)V // ????

 HANDLE NAME add // the handle - in this case, a method reference, being invoked
 HANDLE DESC (D)D // the type of the method being invoked
 HANDL OWNER com/mastfrog/function/state/Dbl
    
 HANDLE  TAG9
 IFACE true
     */
//        Dbl dbl = Dbl.create();
//        fakeStats.forEach(dbl::add);
//        return dbl.getAsDouble() / fakeStats.size();
/*
    
    public void forEach(DoubleConsumer c) {
        for (int i = 0; i < values.length; i++) {
            c.accept(values[i]);
        }
    }    
     */
    /**
     * The tag portion of a method handle.
     *
     * @see
     * https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.4.8
     */
    public static final class TargetKind {

        public static final TargetKind GET_FIELD = new TargetKind(Opcodes.H_GETFIELD, "getField");
        public static final TargetKind GET_STATIC = new TargetKind(Opcodes.H_GETSTATIC, "getStatic");
        public static final TargetKind PUT_FIELD = new TargetKind(Opcodes.H_PUTFIELD, "putField");
        public static final TargetKind PUT_STATIC = new TargetKind(Opcodes.H_PUTSTATIC, "putStatic");
        public static final TargetKind INVOKE_VIRTUAL = new TargetKind(Opcodes.H_INVOKEVIRTUAL,
                "invokeVirtual");
        public static final TargetKind INVOKE_INTERFACE = new TargetKind(Opcodes.H_INVOKEINTERFACE,
                "invokeInterface");
        public static final TargetKind INVOKE_STATIC = new TargetKind(Opcodes.H_INVOKESTATIC,
                "invokeStatic");
        public static final TargetKind INVOKE_SPECIAL = new TargetKind(Opcodes.H_INVOKESPECIAL,
                "invokeSpecial");
        public static final TargetKind NEW_INVOKE_SPECIAL = new TargetKind(Opcodes.H_NEWINVOKESPECIAL,
                "newInvokeSpecial");

        private final int tag;
        private final String name;

        private TargetKind(int tag, String name) {
            this.tag = tag;
            this.name = name;
        }

        public static TargetKind targetKind(int tag) {
            switch (tag) {
                case H_GETFIELD:
                    return GET_FIELD;
                case H_GETSTATIC:
                    return GET_STATIC;
                case H_PUTFIELD:
                    return PUT_FIELD;
                case H_PUTSTATIC:
                    return PUT_STATIC;
                case H_INVOKEVIRTUAL:
                    return INVOKE_VIRTUAL;
                case H_INVOKESTATIC:
                    return INVOKE_STATIC;
                case H_INVOKESPECIAL:
                    return INVOKE_SPECIAL;
                case H_NEWINVOKESPECIAL:
                    return NEW_INVOKE_SPECIAL;
                case H_INVOKEINTERFACE :
                    return INVOKE_INTERFACE;
                default:
                    return new TargetKind(tag, "Unknown");
            }
        }

        public int intValue() {
            return tag;
        }

        public String name() {
            return name;
        }

        public String toString() {
            return name + "(" + tag + ")";
        }

    }
}
