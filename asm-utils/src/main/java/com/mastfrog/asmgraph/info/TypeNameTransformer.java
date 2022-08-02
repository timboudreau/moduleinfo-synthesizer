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
package com.mastfrog.asmgraph.info;

import com.mastfrog.asmgraph.asm.model.TypeName;
import java.util.function.Function;

/**
 * A function that can convert a type name to a string for different purposes
 * (such as printing a method signature with or without package names or
 * generics or as java source format versus machine format). Adds a few methods
 * to the raw function type, which allow consumers to determine what kind of
 * data to use to generate a result (for example, method jvm metadata has a
 * simple raw "descriptor" plus am optional, much more complex,
 * generics-preserving "signature" - if we're returning raw names, using the
 * "descriptor" means we don't have to elide material that is irrelevant).
 *
 * @author Tim Boudreau
 */
public interface TypeNameTransformer extends Function<TypeName, String> {

    /**
     * Returns a default argument name synthesizer for printing method
     * signatures which generates distinct argument names related to the type
     * name which are legal java identifiers.
     *
     * @return A function
     */
    public static Function<TypeName, String> argumentNameSynthesizer() {
        return new ArgNameSynthesizer();
    }

    /**
     * Returns true if the returned name contains complete type information
     * (all generics, package names, etc. intact).
     *
     * @return True if the name is complete
     */
    default boolean isComplete() {
        return false;
    }

    /**
     * Returns true if the returned name will incorporate (package-qualified or
     * not) generics.
     * @return True if the name can include generics
     */
    default boolean includesGenerics() {
        return true;
    }

    /**
     * Returns true if the returned name will include the java package name of
     * the type.
     *
     * @return True if packages may be present
     */
    default boolean includesPackages() {
        return true;
    }

    /**
     * Returns true if the returned names are JVM-internal format (e.g.
     * may be bracketed in L and ; for object types, or use single character
     * type names such as Z for boolean or I for int).
     *
     * @return Whether or not the returned value is not source-code-like.F
     */
    default boolean isMachineFormat() {
        return false;
    }
}
