/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mastfrog.asmgraph.miniparser;

/**
 *
 * @author timb
 */
public interface CharPredicate {
    
    public static CharPredicate FALSE = ch -> false;

    boolean test(char c);

    static CharPredicate of(char... chars) {
        return new CharPredicateImpl(chars);
    }

    static CharPredicate of(char ch) {
        return c -> ch == c;
    }

    default CharPredicate or(CharPredicate other) {
        return c -> this.test(c) || other.test(c);
    }
    
}
