package com.mastfrog.asmgraph.asm.model;

/**
 *
 * @author timb
 */
public enum Captures {

    ANY('*', "?"),
    EXTENDS('+', "extends"),
    SUPER('-', "super");

    private final char value;
    private final String sourceForm;

    Captures(char value, String sourceForm) {
        this.value = value;
        this.sourceForm = sourceForm;
    }

    public char prefix() {
        return value;
    }
    
    public String sourceName() {
        return sourceForm;
    }

    public static Captures match(char c) {
        switch (c) {
            case '*':
                return ANY;
            case '+':
                return EXTENDS;
            case '-':
                return SUPER;
            default:
                return null;
        }
    }

    public char internalPrefix() {
        return value;
    }

    public boolean matches(char c) {
        return value == c;
    }

    public boolean requiresArgument() {
        return this != ANY;
    }
}
