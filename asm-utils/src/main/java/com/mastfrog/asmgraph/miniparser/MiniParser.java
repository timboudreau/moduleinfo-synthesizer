package com.mastfrog.asmgraph.miniparser;

/**
 *
 * @author timb
 */
public interface MiniParser<T> {
    
    default T parse(String text) {
        return parse(new Sequence(text));
    }

    T parse(Sequence text);
    
}
