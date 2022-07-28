package com.mastfrog.asmgraph.asm.model;

import java.util.Optional;

/**
 * Context for reifying generic names into their type specification.
 *
 * @author Tim Boudreau
 */
@FunctionalInterface
public interface GenericsContext {

    /**
     * Get the type specification for a generic name.
     * 
     * @param name The name of the generic
     * @return The type, if one can be found
     */
    Optional<TypeName> typeOf(String name);

    /**
     * Create a GenericsContext that nests this instance underneath a parent
     * instance, such that this instance is queried first and the parent if
     * no answer was found in this instance.
     * 
     * @param parent A parent generics context
     * @return A combined generics context
     */
    default GenericsContext under(GenericsContext parent) {
        assert parent != this;
        return name -> {
            return typeOf(name).or(() -> parent.typeOf(name));
        };
    }
}
