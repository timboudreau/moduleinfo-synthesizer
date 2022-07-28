package com.mastfrog.asmgraph.miniparser;

import java.util.Arrays;

/**
 *
 * @author timb
 */
class CharPredicateImpl implements CharPredicate {
    
    final char[] chars;

    public CharPredicateImpl(char... ch) {
        this.chars = Arrays.copyOf(ch, ch.length);
    }

    @Override
    public boolean test(char c) {
        return Arrays.binarySearch(chars, c) >= 0;
    }
    
}
