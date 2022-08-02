package com.mastfrog.asmgraph.asm.model;

import static com.mastfrog.util.preconditions.Checks.notNull;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author timb
 */
public final class RawTypeName extends TypeName {

    public static final RawTypeName OBJECT = new RawTypeName("java/lang/Object");
    public static final RawTypeName STRING = new RawTypeName("java/lang/String");
    static RawTypeName INTEGER = new RawTypeName("java/lang/Integer");
    static RawTypeName FLOAT = new RawTypeName("java/lang/Float");
    static RawTypeName LONG = new RawTypeName("java/lang/Long");
    static RawTypeName DOUBLE = new RawTypeName("java/lang/Double");
    static RawTypeName BYTE = new RawTypeName("java/lang/Byte");
    static RawTypeName BOOLEAN = new RawTypeName("java/lang/Boolean");
    static RawTypeName CHAR = new RawTypeName("java/lang/Character");
    static RawTypeName SHORT = new RawTypeName("java/lang/Short");
    static RawTypeName VOID = new RawTypeName("java/lang/Void");
    private final String rawName;

    private RawTypeName(String rawName) {
        this.rawName = notNull("rawName", rawName);
        if (rawName.startsWith("Ljava/")) {
            throw new IllegalArgumentException("Leading L should not be part of a simple type name.");
        }
    }

    public static RawTypeName of(String rawName) {
        // Handle a few common cases that can save thousands of allocations
        // per run
        switch (rawName) {
            case "java/lang/Object":
                return OBJECT;
            case "java/lang/String":
                return STRING;
            case "java/lang/Integer":
                return INTEGER;
            case "java/lang/Float":
                return FLOAT;
            case "java/lang/Double":
                return DOUBLE;
            case "java/lang/Byte":
                return BYTE;
            case "java/lang/Boolean":
                return BOOLEAN;
            case "java/lang/Character":
                return CHAR;
            case "java/lang/Short":
                return SHORT;
            case "java/lang/Void":
                return VOID;
            default:
                return new RawTypeName(rawName);
        }
    }

    @Override
    public String javaPackage() {
        int ix = rawName.lastIndexOf('/');
        if (ix >= 0 || ix == rawName.length() - 1) {
            return rawName.substring(0, ix).replace('/', '.');
        }
        return "";
    }

    @Override
    public String sourceNameTruncated() {
        if (rawName.startsWith("java/lang/")) {
            return rawName.substring("java/lang/".length());
        }
        return sourceName();
    }

    @Override
    public RawTypeName transform(Function<String, String> f) {
        String result = f.apply(rawName);
        if (result == null || result.equals(rawName)) {
            return this;
        }
        return new RawTypeName(result);
    }

    @Override
    public String simpleName() {
        int ix = rawName.lastIndexOf('/');
        if (ix < 0 || ix == rawName.length() - 1) {
            return rawName;
        }
        return rawName.substring(ix + 1).replace('$', '.');
    }

    @Override
    public String nameBase() {
        return rawName;
    }

    @Override
    public String internalName() {
        return nameBase();
    }

    @Override
    public String sourceName() {
        return rawName.replace('/', '.').replace('$', '.');
    }

    @Override
    public void visitTypeNames(Consumer<TypeName> c) {
        c.accept(this);
    }

    @Override
    public TypeKind kind() {
        return TypeKind.SIMPLE_OBJECT_TYPE;
    }

    public boolean isRawTypeName() {
        return false;
    }

    @Override
    protected void visitChildren(int depth, TypeVisitor vis, int semanticDepth) {
        // do nothing
    }
}
