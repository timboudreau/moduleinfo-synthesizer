package com.mastfrog.asmgraph.asm.model;

/**
 *
 * @author timb
 */
public class TypeParameter {

    private final String name;
    private final TypeName type;

    public TypeParameter(String name, TypeName type) {
        this.name = name;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public TypeName typeName() {
        return type;
    }

    @Override
    public String toString() {
        return name + ":" + type;
    }
}
