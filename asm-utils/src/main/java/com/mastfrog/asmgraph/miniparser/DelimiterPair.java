package com.mastfrog.asmgraph.miniparser;

/**
 * Defines a pair of delimiters such as parentheses.
 *
 * @author timb
 */
public final class DelimiterPair {

    private final char open;
    private final char close;

    public static final DelimiterPair PARENS = new DelimiterPair('(', ')');
    public static final DelimiterPair ANGLES = new DelimiterPair('<', '>');

    public DelimiterPair(char open, char close) {
        this.open = open;
        this.close = close;
    }

    public boolean isStart(char c) {
        return c == open;
    }

    public boolean isEnd(char c) {
        return c == close;
    }

    public CharPredicate open() {
        return this::isStart;
    }

    public CharPredicate close() {
        return this::isEnd;
    }

    @Override
    public String toString() {
        return new String(new char[]{open, close});
    }

    /**
     * Takes a stop predicate which should be disabled when within some
     * delimiters, and returns a single-use wrapper predicate which will not
     * return true if at least one opening delimiter has been passed but no
     * corresponding close delimiter has been.
     *
     * @param pred A predicate
     * @return A predicate
     */
    public CharPredicate blocking(CharPredicate pred) {
        return new StatefulCharPredicate(pred, this);
    }

    public static CharPredicate blockingWith(CharPredicate pred, DelimiterPair... pairs) {
        for (DelimiterPair d : pairs) {
            pred = d.blocking(pred);
        }
        return pred;
    }

    class StatefulCharPredicate implements CharPredicate {

        private final CharPredicate orig;
        private final DelimiterPair delims;
        private int opens;

        public StatefulCharPredicate(CharPredicate orig, DelimiterPair delims) {
            this.orig = orig;
            this.delims = delims;
        }

        @Override
        public boolean test(char c) {
            if (delims.isStart(c)) {
                opens++;
                return false;
            }
            if (opens > 0) {
                return false;
            }
            if (delims.isEnd(c) && opens > 0) {
                return false;
            }
            return orig.test(c);
        }

    }
}
