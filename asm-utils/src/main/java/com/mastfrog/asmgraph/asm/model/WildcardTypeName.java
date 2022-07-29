/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mastfrog.asmgraph.asm.model;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author timb
 */
public class WildcardTypeName extends TypeName {

    public static final WildcardTypeName WILDCARD = new WildcardTypeName();

    private WildcardTypeName() {
        // do nothing
    }

    @Override
    public String sourceNameTruncated() {
        return "?";
    }

    @Override
    public String rawName() {
        return "*";
    }

    @Override
    public String sourceName() {
        return "?";
    }

    @Override
    public String internalName() {
        return "*";
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
        return TypeKind.WILDCARD;
    }

    @Override
    protected void visitChildren(int depth, TypeVisitor vis) {
        // do nothing
    }
}
