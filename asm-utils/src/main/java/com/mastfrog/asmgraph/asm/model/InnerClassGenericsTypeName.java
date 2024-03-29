/*
 * The MIT License
 *
 * Copyright 2022 Mastfrog Technologies.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mastfrog.asmgraph.asm.model;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author timb
 */
public class InnerClassGenericsTypeName extends TypeName {

    private final TypeName firstSection;
    private final String innerType;

    public InnerClassGenericsTypeName(TypeName firstSection, String innerType) {
        this.firstSection = firstSection;
        this.innerType = innerType;
    }

    public String tail() {
        return innerType;
    }

    @Override
    public TypeName rawName() {
        TypeName tn = firstSection.rawName();
        if (!tn.equals(firstSection)) {
            return new InnerClassGenericsTypeName(tn, innerType);
        }
        return this;
    }

    @Override
    public String sourceNameTruncated() {
        String sn = firstSection.sourceNameTruncated();
        String full = firstSection.sourceName();
        if (full.equals(sn)) {
            return full + "." + innerType;
        }
        return sn + "." + innerType;
    }

    @Override
    public boolean isSemantic() {
        return true;
    }

    @Override
    public boolean isRawTypeName() {
        return firstSection.isRawTypeName();
    }

    @Override
    public boolean isFullySpecified() {
        return firstSection.isFullySpecified();
    }

    @Override
    public Optional<TypeName> reify(GenericsContext ctx) {
        return firstSection.reify(ctx).map(
                tn -> new InnerClassGenericsTypeName(tn, innerType));
    }

    @Override
    public String javaPackage() {
        return firstSection.javaPackage();
    }

    @Override
    public String simpleName() {
        return innerType;
    }

    @Override
    public String nameBase() {
        return firstSection.nameBase() + '.' + innerType;
    }

    @Override
    public String sourceName() {
        return firstSection.sourceName() + '.' + innerType;
    }

    @Override
    public TypeKind kind() {
        return TypeKind.PARAMETERIZED_OBJECT_TYPE;
    }

    @Override
    public TypeName transform(Function<String, String> f) {
        return new InnerClassGenericsTypeName(firstSection.transform(f),
                f.apply(innerType));
    }

    @Override
    protected void visitChildren(int depth, TypeVisitor vis, int semanticDepth) {
        firstSection.accept(Optional.of(this), semanticDepth + 1, TypeVisitor.TypeNesting.INNER_CLASS_OF,
                depth, vis);
    }

    @Override
    public void visitTypeNames(Consumer<TypeName> c) {
        c.accept(this);
    }
}
