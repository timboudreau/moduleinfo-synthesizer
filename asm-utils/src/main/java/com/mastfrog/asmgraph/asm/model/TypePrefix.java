package com.mastfrog.asmgraph.asm.model;

import java.util.Arrays;

/**
 *
 * @author timb
 */
public enum TypePrefix {
    ARRAY('['),
    CAPTURE_OF('-'),
    INTERSECTION('+'),
    OBJECT('L');

    private final char prefix;

    private TypePrefix(char prefix) {
        this.prefix = prefix;
    }
    
    public TypeKind kind() {
        switch(this) {
            case ARRAY :
                return TypeKind.ARRAY;
            case CAPTURE_OF :
                return TypeKind.CAPTURE;
            case INTERSECTION :
                return TypeKind.INTERSECTION;
            case OBJECT :
                return TypeKind.SIMPLE_OBJECT_TYPE;
            default :
                throw new AssertionError(this);
        }
    }

    public boolean requiresObjectType() {
        return this != ARRAY;
    }

    public boolean canHaveFollowingPrefixes() {
        return this != OBJECT;
    }

    public boolean takesParameters() {
        return this == CAPTURE_OF || this == INTERSECTION || this == OBJECT;
    }

    public String sourceFormat(TypeName sourceName, TypeName... parameters) {
        if (!takesParameters() && parameters.length > 0) {
            throw new IllegalArgumentException(name()
                    + " does not take parameters, but was passed "
                    + Arrays.toString(parameters));
        }
        switch (this) {
            case ARRAY:
                return sourceName.sourceName() + "[]";
            case OBJECT:
                String base = sourceName.rawName().replace('/', '.').replace('$', '.');
                if (parameters.length > 0) {
                    base += "<";
                    for (int i = 0; i < parameters.length; i++) {
                        base += parameters[i].sourceName();
                        if (i != parameters.length - 1) {
                            base += ",";
                        }
                    }
                }
                return base;
            default:
                throw new AssertionError(this);

        }
    }

    public String suffix() {
        switch (this) {
            case OBJECT:
                return ";";
            default:
                return "";
        }
    }

    public char prefix() {
        return prefix;
    }

    public static TypePrefix matching(char c) {
        switch (c) {
            case '[':
                return ARRAY;
            case '!':
                return CAPTURE_OF;
            case '|':
                return INTERSECTION;
            case 'L':
                return OBJECT;
        }
        return null;
    }

}
