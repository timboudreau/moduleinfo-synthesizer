package com.mastfrog.asmgraph.asm.model;

import static com.mastfrog.asmgraph.asm.model.TypeKind.TYPE_PARAMETER;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author timb
 */
public class ArrayTypeName extends TypeName {

    private final TypeName componentType;

    public ArrayTypeName(TypeName componentType) {
        this.componentType = componentType;
    }

    @Override
    public String javaPackage() {
        return componentType.javaPackage();
    }

    @Override
    public String simpleName() {
        return componentType.simpleName() + "[]";
    }

    @Override
    public TypeName rawName() {
        TypeName result = componentType.rawName();
        if (result == componentType) {
            return this;
        }
        return new ArrayTypeName(result);
    }

    @Override
    public String sourceNameTruncated() {
        return componentType.sourceNameTruncated() + "[]";
    }

    @Override
    public Optional<TypeName> reify(GenericsContext ctx) {
        return componentType.reify(ctx).map(nue -> new ArrayTypeName(nue));
    }

    @Override
    protected void visitChildren(int depth, TypeVisitor vis, int semanticDepth) {
        componentType.accept(Optional.of(this), semanticDepth + 1,
                TypeVisitor.TypeNesting.ARRAY_COMPONENT, depth, vis);
    }

    public boolean isPrimitiveArray() {
        if (componentType.kind() == TypeKind.ARRAY) {
            return ((ArrayTypeName) componentType).isPrimitiveArray();
        }
        return !componentType.kind().isObject();
    }

    @Override
    public TypeKind kind() {
        return TypeKind.ARRAY;
    }

    @Override
    public void visitTypeNames(Consumer<TypeName> c) {
        c.accept(this);
        componentType.visitTypeNames(c);
    }

    @Override
    public String nameBase() {
        return '[' + componentType.nameBase();
    }

    @Override
    public String sourceName() {
        return componentType.sourceName() + "[]";
    }

    @Override
    public String internalName() {
        if (isPrimitiveArray()) {
            return nameBase();
        }
        if (componentType.kind().canTakeGenerics() || componentType.kind() == TYPE_PARAMETER || componentType.kind().isObject()) {
            return nameBase() + ";";
        }
        return nameBase();
    }

    @Override
    public TypeName transform(Function<String, String> f) {
        TypeName result = componentType.transform(f);
        if (result != componentType) {
            return new ArrayTypeName(result);
        }
        return this;
    }

}
