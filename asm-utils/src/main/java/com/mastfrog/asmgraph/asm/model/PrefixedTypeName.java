package com.mastfrog.asmgraph.asm.model;

import com.mastfrog.asmgraph.asm.model.TypeVisitor.TypeNesting;
import static com.mastfrog.util.preconditions.Checks.notNull;
import java.util.Optional;
import static java.util.Optional.of;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author timb
 */
public class PrefixedTypeName extends TypeName {

    private final TypePrefix prefix;

    private final TypeName child;

    public PrefixedTypeName(TypePrefix prefix, TypeName child) {
        this.prefix = notNull("prefix", prefix);
        this.child = notNull("child", child);
    }

    @Override
    public Optional<TypeName> reify(GenericsContext ctx) {
        return child.reify(ctx).map(nue -> new PrefixedTypeName(prefix, nue));
    }

    @Override
    public TypeKind kind() {
        return prefix.kind();
    }

    public boolean isSemantic() {
        return prefix != TypePrefix.OBJECT;
    }

    @Override
    public String rawName() {
        return prefix.prefix() + child.rawName();
    }

    @Override
    public String sourceName() {
//        switch(prefix) {
//            case OBJECT :
//                return child.sourceName();
//        }
        return prefix.sourceFormat(child);
    }

    @Override
    public String internalName() {
        return prefix.prefix() + child.internalName() + prefix.suffix();
    }

    @Override
    public TypeName transform(Function<String, String> f) {
        TypeName nue = child.transform(f);
        if (nue != null && !nue.equals(child)) {
            return new PrefixedTypeName(prefix, nue);
        }
        return this;
    }

    @Override
    public void visitTypeNames(Consumer<TypeName> c) {
        c.accept(this);
        child.visitTypeNames(c);
    }

    @Override
    protected void visitChildren(int depth, TypeVisitor vis) {
        child.accept(of(this), TypeNesting.WRAPPED, depth, vis);
    }
}
