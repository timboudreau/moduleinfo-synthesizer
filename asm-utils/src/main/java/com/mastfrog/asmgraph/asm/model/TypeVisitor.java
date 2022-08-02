package com.mastfrog.asmgraph.asm.model;

import java.util.Optional;

/**
 * Visitor for types.
 *
 * @author Tim Boudreau
 */
public interface TypeVisitor {

    void visit(Optional<TypeName> parent, int semanticDepth, TypeNesting nesting, TypeName target, int depth);

    /**
     * The purpose for which a TypeName is nested underneath another.
     */
    public enum TypeNesting {
        /**
         * This is the outermost type.
         */
        SELF,
        /**
         * This parent type wraps this type for some reason that have a semantic
         * impact on the way it is composed into a signature (for example,
         * prepending the leading colon used by interfaces in generic
         * signatures) but does not change the semantics of the type as used in
         * java code (does not turn it in to an array, add generics, or
         * whatever).
         */
        WRAPPED,
        /**
         * This type is a type parameter of the parent.
         */
        TYPE_PARAMETER,
        /**
         * This type is nested under a parent that applies a capture argument
         * (e.g. <code>? super</code> or <code>? extends</code> to its
         * contents).
         */
        APPLY_CAPTURE,
        /**
         * This type is a peer of its siblings, as in intersection types or
         * lists of interfaces in an <code>implements</code> clause.
         */
        PEER,
        /**
         * The parent of this type name uses this type name as its array
         * component element (note in multi-dimensional arrays, this may still
         * indicate TypeName with the kind ARRAY and further ARRAY_COMPONENT
         * children.
         */
        ARRAY_COMPONENT,
        /**
         * Type parameters when inner classes of an outer class that has
         * generics have a peculiar dot-delimited syntax, e.g.
         * <pre>
         * com/foo/InnerClassGenerics&lt;TK;TV;&gt;.Entry.Sub.SubValue&lt;TR;&gt;.SubSubValue.SubSubSubValue
         * </pre>
         */
        INNER_CLASS_OF,
    }
}
