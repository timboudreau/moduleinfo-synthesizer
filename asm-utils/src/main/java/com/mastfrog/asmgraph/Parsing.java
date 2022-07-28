package com.mastfrog.asmgraph;

import com.mastfrog.asmgraph.parsing.ClassSignatureParser;
import com.mastfrog.asmgraph.parsing.MethodSignatureParser;
import com.mastfrog.asmgraph.parsing.TypeParser;
import com.mastfrog.asmgraph.asm.model.ClassSignature;
import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import static com.mastfrog.util.preconditions.Checks.notNull;

/**
 * Provides access to the parsing infrastructure without creating a direct
 * dependency from the caller to it.
 *
 * @author Tim Boudreau
 */
public final class Parsing {

    private Parsing() {
        throw new AssertionError();
    }

    /**
     * Parse a field signature.
     *
     * @param sig A signature string
     * @return A type name
     * @throws IllegalArgumentException if the content is invalid
     */
    public static TypeName fieldSignature(String sig) {
        return new TypeParser().parse(notNull("sig", sig));
    }

    /**
     * Parse a class signature.
     *
     * @param sig A signature
     * @return A class signature
     * @throws IllegalArgumentException if the content is invalid
     */
    public static ClassSignature classSignature(String sig) {
        return new ClassSignatureParser().parse(notNull("sig", sig));
    }

    /**
     * Parse a method signature.
     *
     * @param sig A signature string
     * @return A method signature
     */
    public static MethodSignature methodSignature(String sig) {
        return new MethodSignatureParser().parse(notNull("sig", sig));
    }
}
