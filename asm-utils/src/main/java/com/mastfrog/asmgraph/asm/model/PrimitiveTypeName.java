package com.mastfrog.asmgraph.asm.model;

import static com.mastfrog.util.preconditions.Checks.notNull;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author timb
 */
public final class PrimitiveTypeName extends TypeName {

    private final PrimitiveTypes type;

    public PrimitiveTypeName(PrimitiveTypes type) {
        this.type = notNull("type", type);
    }

    @Override
    public String sourceNameTruncated() {
        return sourceName();
    }

    @Override
    public String simpleName() {
        return type.name().toLowerCase();
    }

    @Override
    public String javaPackage() {
        return "";
    }

    @Override
    public String nameBase() {
        return Character.toString(type.prefix());
    }

    @Override
    public String sourceName() {
        return type.name().toLowerCase();
    }

    @Override
    public TypeName transform(Function<String, String> f) {
        return this;
    }

    @Override
    public void visitTypeNames(Consumer<TypeName> c) {
        c.accept(this);
    }

    @Override
    public TypeKind kind() {
        return TypeKind.PRIMITIVE;
    }

    @Override
    protected void visitChildren(int depth, TypeVisitor vis, int semanticDepth) {
        // do nothing
    }
}
