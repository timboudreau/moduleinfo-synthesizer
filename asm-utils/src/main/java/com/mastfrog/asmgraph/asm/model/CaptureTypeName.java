package com.mastfrog.asmgraph.asm.model;

import com.mastfrog.asmgraph.asm.model.TypeVisitor.TypeNesting;
import static com.mastfrog.asmgraph.asm.model.TypeVisitor.TypeNesting.APPLY_CAPTURE;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author timb
 */
public class CaptureTypeName extends TypeName {

    private final Captures captureType;
    private final TypeName of;

    public CaptureTypeName(Captures captureType, TypeName of) {
        this.captureType = captureType;
        this.of = of;
    }

    @Override
    public TypeName rawName() {
        return of;
    }

    @Override
    public String javaPackage() {
        return of.javaPackage();
    }

    @Override
    public String sourceNameTruncated() {
        String sn = of.sourceName();
        String snt = of.sourceNameTruncated();
        if (snt.equals(sn)) {
            return sourceName();
        }
        return new CaptureTypeName(captureType, TypeName.simpleName(snt)).sourceName();
    }

    @Override
    public Optional<TypeName> reify(GenericsContext ctx) {
        return of.reify(ctx).map(nue -> new CaptureTypeName(captureType, nue));
    }

    @Override
    protected void visitChildren(int depth, TypeVisitor vis, int semanticDepth) {
        of.accept(Optional.of(this), semanticAddition(), APPLY_CAPTURE, depth, vis);
    }

    @Override
    public TypeKind kind() {
        return of.kind();
    }

    @Override
    public void visitTypeNames(Consumer<TypeName> c) {
        c.accept(this);
        of.visitTypeNames(c);
    }

    @Override
    public String nameBase() {
        return captureType.prefix() + of.internalName();
    }

    @Override
    public String sourceName() {
        return "? " + captureType.sourceName() + " " + of.sourceName();
    }

    @Override
    public String simpleName() {
        return "? " + captureType.sourceName() + " " + of.simpleName();
    }

    @Override
    public TypeName transform(Function<String, String> f) {
        TypeName tn = of.transform(f);
        if (tn == null || tn.equals(of)) {
            return this;
        }
        return new CaptureTypeName(captureType, tn);
    }
}
