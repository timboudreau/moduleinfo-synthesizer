package com.mastfrog.asmgraph.asm.model;

import static com.mastfrog.util.preconditions.Checks.notNull;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * In the generic type specification for a method, interface types have an extra
 * : prefix, probably to make it easy for tooling to tell when a type
 * intersection with &amp; is legal or not (must be an interface). This
 * represents that type. It is not used in generic <i>arguments</i>
 * which have no such metadata.
 *
 * @author Tim Boudreau
 */
public class InterfaceType extends TypeName {

    private final TypeName inner;

    public InterfaceType(TypeName inner) {
        this.inner = notNull("inner", inner);
        if (inner instanceof InterfaceType) {
            throw new IllegalArgumentException("Already an interface type: " + inner);
        }
    }

    @Override
    public String javaPackage() {
        return inner.javaPackage();
    }

    @Override
    public String simpleName() {
        return inner.simpleName();
    }

    @Override
    public TypeName rawName() {
        TypeName tn = inner.rawName();
        if (inner == tn) {
            return this;
        }
        return new InterfaceType(tn);
    }

    @Override
    public String sourceNameTruncated() {
        return inner.sourceNameTruncated();
    }

    @Override
    public Optional<TypeName> reify(GenericsContext ctx) {
        return inner.reify(ctx).map(nue -> new InterfaceType(nue));
    }

    @Override
    protected void visitChildren(int depth, TypeVisitor vis, int semanticDepth) {
        inner.accept(Optional.of(this), semanticDepth, TypeVisitor.TypeNesting.WRAPPED, depth, vis);
    }

    public boolean isSemantic() {
        return false;
    }

    @Override
    public TypeKind kind() {
        return inner.kind();
    }

    public static TypeName of(TypeName what) {
        if (what instanceof InterfaceType) {
            return what;
        }
        return new InterfaceType(what);
    }

    @Override
    public String nameBase() {
        return ":" + inner.nameBase();
    }

    @Override
    public String sourceName() {
        return inner.sourceName();
    }

    @Override
    public String internalName() {
        String n = nameBase();
        if (!n.endsWith(";")) {
            return n + ";";
        }
        return n;
    }

    @Override
    public TypeName transform(Function<String, String> f) {
        TypeName res = inner.transform(f);
        if (res == null || res.equals(inner)) {
            return this;
        }
        return new InterfaceType(res);
    }

    @Override
    public void visitTypeNames(Consumer<TypeName> c) {
        c.accept(this);
        inner.visitTypeNames(c);
    }

}
