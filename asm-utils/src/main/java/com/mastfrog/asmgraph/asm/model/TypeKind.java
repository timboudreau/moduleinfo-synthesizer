package com.mastfrog.asmgraph.asm.model;

import java.util.Optional;
import static java.util.Optional.*;

/**
 *
 * @author timb
 */
public enum TypeKind {
    PRIMITIVE,
    SIMPLE_OBJECT_TYPE,
    ARRAY,
    CAPTURE,
    INTERSECTION,
    PARAMETERIZED_OBJECT_TYPE,
    WILDCARD,
    TYPE_PARAMETER;

    public String stringPrefix() {
        return prefix().map(pfx -> Character.toString(pfx.prefix())).orElse("");
    }
    
    public boolean isObject() {
        switch(this) {
            case PRIMITIVE :
                return false;
            default :
                return true;
        }
    }

    public boolean canTakeGenerics() {
        switch (this) {
            case SIMPLE_OBJECT_TYPE:
            case CAPTURE:
            case INTERSECTION:
            case PARAMETERIZED_OBJECT_TYPE:
                return true;
            default:
                return false;
        }
    }

    Optional<TypePrefix> prefix() {
        switch (this) {
            case PRIMITIVE:
            case TYPE_PARAMETER :
            case WILDCARD :
                return empty();
            case ARRAY:
                return of(TypePrefix.ARRAY);
            case CAPTURE:
                return of(TypePrefix.CAPTURE_OF);
            case INTERSECTION:
                return of(TypePrefix.INTERSECTION);
            case SIMPLE_OBJECT_TYPE:
            case PARAMETERIZED_OBJECT_TYPE:
                return of(TypePrefix.OBJECT);
            default:
                throw new AssertionError(this);
        }
    }
}
