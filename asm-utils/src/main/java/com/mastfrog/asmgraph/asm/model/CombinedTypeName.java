package com.mastfrog.asmgraph.asm.model;

import static com.mastfrog.util.preconditions.Checks.noNullElements;
import static com.mastfrog.util.preconditions.Checks.notNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author timb
 */
public class CombinedTypeName extends TypeName {

    private final List<TypeName> contents;

    public CombinedTypeName(List<TypeName> contents) {
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("Empty type list");
        }
        noNullElements("contents", notNull("contents", contents), contents.toArray());
        this.contents = contents;
    }

    @Override
    public TypeName rawName() {
        return contents.get(0);
    }

    @Override
    public String javaPackage() {
        // Hmm...
        return contents.get(0).javaPackage();
    }

    @Override
    public Optional<TypeName> reify(GenericsContext ctx) {
        List<TypeName> nue = new ArrayList<>(contents.size());
        for (TypeName tn : contents) {
            nue.add(tn.reify(ctx).orElse(tn));
        }
        if (!nue.equals(contents)) {
            return Optional.of(new CombinedTypeName(nue));
        }
        return Optional.empty();
    }

    @Override
    protected void visitChildren(int depth, TypeVisitor vis, int semanticDepth) {
        Optional<TypeName> me = Optional.of(this);
        for (TypeName ct : contents) {
            ct.accept(me, semanticDepth + 1, TypeVisitor.TypeNesting.PEER, depth, vis);
        }
    }

    @Override
    public TypeKind kind() {
        return TypeKind.INTERSECTION;
    }

    public static TypeName from(List<TypeName> contents) {
        if (contents.size() == 1) {
            return contents.get(0);
        }
        return new CombinedTypeName(contents);
    }

    @Override
    public String nameBase() {
        StringBuilder sb = new StringBuilder();
        for (TypeName t : contents) {
            sb.append(t);
        }
        return sb.toString();
    }

    @Override
    public String sourceName() {
        StringBuilder sb = new StringBuilder();
        for (TypeName tn : contents) {
            if (sb.length() > 0) {
                sb.append(" & ");
            }
            sb.append(tn.sourceName());
        }
        return sb.toString();
    }

    @Override
    public String simpleName() {
        StringBuilder sb = new StringBuilder();
        for (TypeName tn : contents) {
            if (sb.length() > 0) {
                sb.append(" & ");
            }
            sb.append(tn.simpleName());
        }
        return sb.toString();
    }

    @Override
    public String sourceNameTruncated() {
        StringBuilder sb = new StringBuilder();
        for (TypeName tn : contents) {
            if (sb.length() > 0) {
                sb.append(" & ");
            }
            sb.append(tn.sourceNameTruncated());
        }
        return sb.toString();
    }

    @Override
    public String internalName() {
        StringBuilder sb = new StringBuilder();
        for (TypeName tn : contents) {
            sb.append(tn.internalName());
        }
        return sb.toString();
    }

    @Override
    public TypeName transform(Function<String, String> f) {
        List<TypeName> nc = new ArrayList<>(contents.size());
        for (TypeName tn : contents) {
            nc.add(tn.transform(f));
        }
        if (nc.equals(contents)) {
            return this;
        }
        return new CombinedTypeName(nc);
    }

    @Override
    public void visitTypeNames(Consumer<TypeName> c) {
        c.accept(this);
        for (TypeName t : contents) {
            t.visitTypeNames(c);
        }
    }

}
