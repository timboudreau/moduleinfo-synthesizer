package com.mastfrog.asmgraph.asm.model;

import static com.mastfrog.util.preconditions.Checks.notNull;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author timb
 */
public class TypeParameterTypeName extends TypeName {
    
    private final String name;

    public TypeParameterTypeName(String name) {
        this.name = notNull("name", name);
    }

    @Override
    public String simpleName() {
        return name;
    }

    @Override
    public String javaPackage() {
        return "";
    }

    @Override
    public String sourceNameTruncated() {
        return name;
    }

    @Override
    public Optional<TypeName> reify(GenericsContext ctx) {
        return ctx.typeOf(name);
    }
    
    @Override
    protected void visitChildren(int depth, TypeVisitor vis, int sem) {
        // do nothing
    }

    @Override
    public String nameBase() {
        return 'T' + name;
    }

    @Override
    public String sourceName() {
        return name;
    }

    @Override
    public String internalName() {
        return nameBase() + ';';
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
        return TypeKind.TYPE_PARAMETER;
    }
    
    
    
    
    
}
