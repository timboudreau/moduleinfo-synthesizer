package com.mastfrog.asmgraph.asm.model;

import java.util.function.Function;

/**
 *
 * @author timb
 */
public enum PrimitiveTypes implements TypeNamed {
    BYTE('B'),
    CHAR('C'),
    DOUBLE('D'),
    FLOAT('F'),
    INT('I'),
    LONG('J'),
    SHORT('S'),
    VOID('V'),
    BOOLEAN('Z'),;
    private final char prefix;
    private final PrimitiveTypeName name;

    private PrimitiveTypes(char prefix) {
        this.prefix = prefix;
        name = new PrimitiveTypeName(this);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    public RawTypeName boxedType() {
        switch (this) {
            case INT:
                return RawTypeName.INTEGER;
            case BYTE:
                return RawTypeName.BYTE;
            case BOOLEAN:
                return RawTypeName.BOOLEAN;
            case CHAR:
                return RawTypeName.CHAR;
            case DOUBLE:
                return RawTypeName.DOUBLE;
            case FLOAT:
                return RawTypeName.FLOAT;
            case LONG:
                return RawTypeName.LONG;
            case SHORT:
                return RawTypeName.SHORT;
            case VOID:
                return RawTypeName.VOID;
            default:
                throw new AssertionError(this);
        }
    }

    public static PrimitiveTypes match(String txt) {
        return txt.length() == 0 ? null : match(txt.charAt(0));
    }

    public static PrimitiveTypes match(char c) {
        switch (c) {
            case 'B':
                return BYTE;
            case 'C':
                return CHAR;
            case 'D':
                return DOUBLE;
            case 'F':
                return FLOAT;
            case 'I':
                return INT;
            case 'J':
                return LONG;
            case 'S':
                return SHORT;
            case 'V':
                return VOID;
            case 'Z':
                return BOOLEAN;
        }
        return null;
    }

    public boolean matches(String text) {
        return text.length() > 0 && matches(text.charAt(0));
    }

    public boolean matches(char c) {
        return prefix == c;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    @Override
    public TypeName typeName() {
        return name;
    }
    
    public char prefix() {
        return prefix;
    }

}
