package com.mastfrog.asmgraph.asm.model;

import com.mastfrog.asmgraph.asm.model.TypeVisitor.TypeNesting;
import static com.mastfrog.util.preconditions.Checks.notNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author timb
 */
public class ParameterizedTypeName extends TypeName {

    private final List<TypeName> generics;
    private final TypeName target;

    public ParameterizedTypeName(TypeName target, List<TypeName> generics) {
        this.target = notNull("target", target);
        if (target instanceof RawTypeName) {
            throw new IllegalStateException("SHould not use RawTypeName directly");
        }
        this.generics = generics;
    }

    @Override
    public Optional<TypeName> reify(GenericsContext ctx) {
        TypeName nue = target.reify(ctx).orElse(target);
        List<TypeName> newGenerics = new ArrayList<>(generics.size());
        generics.forEach(g -> {
            newGenerics.add(g.reify(ctx).orElse(g));
        });
        if (!nue.equals(target) || !generics.equals(newGenerics)) {
            return Optional.of(new ParameterizedTypeName(nue, newGenerics));
        }
        return Optional.empty();
    }

    @Override
    protected void visitChildren(int depth, TypeVisitor vis) {
        Optional<TypeName> me = Optional.of(this);
        target.accept(me, TypeNesting.SELF, depth, vis);
        generics.forEach(g -> {
            g.accept(me, TypeNesting.TYPE_PARAMETER, depth, vis);
        });
    }

    @Override
    public TypeKind kind() {
        return TypeKind.PARAMETERIZED_OBJECT_TYPE;
    }

    @Override
    public void visitTypeNames(Consumer<TypeName> c) {
        c.accept(this);
        c.accept(target);
        for (TypeName g : generics) {
            g.visitTypeNames(c);
        }
    }

    @Override
    public String rawName() {
        StringBuilder sb = new StringBuilder();
        sb.append(target.rawName());
        sb.append('<');
        for (TypeName g : generics) {
            sb.append(g.internalName());
        }
        sb.append(">");
        return sb.toString();
    }

    @Override
    public String internalName() {
        return rawName() + ";";
    }

    @Override
    public String sourceName() {
        StringBuilder sb = new StringBuilder();
        sb.append(target.sourceName());
        sb.append('<');
        for (Iterator<TypeName> it = generics.iterator(); it.hasNext();) {
            TypeName g = it.next();
            sb.append(g.sourceName());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append('>');
        return sb.toString();
    }

    @Override
    public String sourceNameTruncated() {
        StringBuilder sb = new StringBuilder();
        sb.append(target.sourceNameTruncated());
        sb.append('<');
        for (Iterator<TypeName> it = generics.iterator(); it.hasNext();) {
            TypeName g = it.next();
            sb.append(g.sourceNameTruncated());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append('>');
        return sb.toString();
    }

    @Override
    public TypeName transform(Function<String, String> f) {
        TypeName nue = target.transform(f);
        List<TypeName> newGenerics = new ArrayList<>();
        for (TypeName g : generics) {
            newGenerics.add(g.transform(f));
        }
        if (!nue.equals(target) || !newGenerics.equals(generics)) {
            return new ParameterizedTypeName(nue, newGenerics);
        }
        return this;
    }

}
