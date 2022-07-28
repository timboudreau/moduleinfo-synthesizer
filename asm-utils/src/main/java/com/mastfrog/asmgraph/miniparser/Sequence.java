package com.mastfrog.asmgraph.miniparser;

import static java.lang.Integer.max;
import static java.lang.Math.min;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

/*
Example:
<T:Ljava/lang/Object;>(Ljava/util/Collection<TT;>;Ljava/util/Collection<TT;>;)Z
 */
public class Sequence {

    private final String text;
    int position = 0;
    private int limit = -1;

    public Sequence(String s) {
        this.text = s;
    }

    String contents() {
        if (isAtEnd()) {
            return "";
        }
        return text.substring(position, limit());
    }

    @Override
    public String toString() {
        char[] space = new char[text.length() + 1];
        Arrays.fill(space, ' ');
        space[position] = '^';
        if (limit >= 0) {
            space[limit] = 'x';
        }
        return text + "\n" + new String(space);
    }

    public boolean limited(int newEnd, BooleanSupplier code) {
        return limited(position, newEnd, code);
    }

    public boolean limited(int newStart, int newEnd, BooleanSupplier code) {
        int oldPos = position;
        int oldLimit = limit;
        position = newStart;
        limit = newEnd;
        boolean result = code.getAsBoolean();
        if (result) {
            position = newEnd + 1;
            limit = oldLimit;
        } else {
            position = oldPos;
            limit = oldLimit;
        }
        return result;
    }

    public int limit() {
        if (limit >= 0) {
            return limit;
        }
        return text.length();
    }

    public int positionOf(char c) {
        for (int i = position; i < limit(); i++) {
            if (text.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    public Sequence clearLimit() {
        limit = -1;
        return this;
    }

    public int size() {
        return text.length();
    }

    public boolean isDone() {
        return position >= limit();
    }

    public boolean isAtEnd() {
        return position >= limit() - 1;
    }

    public boolean isCompleted() {
        return position >= text.length();
    }

    public int position() {
        return position;
    }

    public char curr() {
        return charAt(position);
    }

    public char get() {
        if (isDone()) {
            return 0;
        }
        int pos = position++;
        return text.charAt(pos);
    }

    public String consumeTo(int end) {
        int oldStart = position;
        if (end == position) {
            return "";
        }
        int realEnd = Math.min(end, limit());
        position = realEnd;
        return text.substring(oldStart, realEnd);
    }

    public boolean provisionally(BooleanSupplier supp) {
        int oldPos = position;
        boolean result = supp.getAsBoolean();
        if (!result) {
            position = oldPos;
        }
        return result;
    }

    public boolean tryConsume(char expected, BooleanSupplier consumer) {
        if (curr() == expected) {
            int newStart = ++position;
            boolean result = consumer.getAsBoolean();
            if (!result) {
                position = newStart - 1;
            }
            return result;
        }
        return false;
    }

    public String scanTo(char target, CharPredicate stop) {
        if (isDone()) {
            return null;
        }
        int pos = position;
        for (int i = pos; i < limit(); i++) {
            char c = text.charAt(i);
            if (stop.test(c)) {
                return null;
            } else if (c == target) {
                position = i;
                String result = text.substring(pos, position);
                position++;
                return result;
            }
        }
        return null;
    }

    public int delimiterScan(char openingDelimiter, char closingDelimiter, char... chars) {
        if (isDone()) {
            return -1;
        }
        return delimiterScan(position, openingDelimiter, closingDelimiter, chars);
    }

    public int delimiterScan(char openingDelimiter, char closingDelimiter, CharPredicate chars) {
        if (isDone()) {
            return -1;
        }
        return delimiterScan(position, openingDelimiter, closingDelimiter, false, chars);
    }

    public int delimiterScan(int start, char openingDelimiter, char closingDelimiter, char... chars) {
        return delimiterScan(start, openingDelimiter, closingDelimiter, false, CharPredicate.of(chars));
    }

    public int delimiterScan(char openingDelimiter, char closingDelimiter, boolean ignoreStopWithinInnerDelimiters, CharPredicate stop) {
        return delimiterScan(position, openingDelimiter, closingDelimiter, ignoreStopWithinInnerDelimiters, stop);
    }

    public int delimiterScan(int start, char openingDelimiter, char closingDelimiter, boolean ignoreStopWithinInnerDelimiters, CharPredicate stop) {
        int innerDelims = 0;
        for (int i = position; i < limit(); i++) {
            char c = text.charAt(i);
            if (stop.test(c)) {
                if (ignoreStopWithinInnerDelimiters && innerDelims > 0) {
                    continue;
                }
                return -1;
            }
            if (c == openingDelimiter) {
                innerDelims++;
            } else if (c == closingDelimiter) {
                if (innerDelims > 0) {
                    innerDelims--;
                } else {
                    return i;
                }
            }
        }
        return -1;
    }

    public int delimiterScan(DelimiterPair pair, CharPredicate stop, DelimiterPair... ignoreStopWhenIn) {
        return delimiterScan(position, pair, stop, ignoreStopWhenIn);
    }

    public int delimiterScan(int start, DelimiterPair pair, CharPredicate stop, DelimiterPair... ignoreStopWhenIn) {
        return delimiterScan(start, pair, DelimiterPair.blockingWith(stop, ignoreStopWhenIn));
    }

    public int delimiterScan(int start, DelimiterPair pair, CharPredicate stop) {
        int innerDelims = 0;
        for (int i = position; i < limit(); i++) {
            char c = text.charAt(i);
            if (stop.test(c)) {
                return -1;
            }
            if (pair.isStart(c)) {
                innerDelims++;
            } else if (pair.isEnd(c)) {
                if (innerDelims > 0) {
                    innerDelims--;
                } else {
                    return i;
                }
            }
        }
        return -1;
    }

    public String substring(int length) {
        int end = Math.min(position + length, limit());
        return text.substring(position, end);
    }

    private char charAt(int position) {
        if (position < 0 || position >= limit()) {
            return 0;
        }
        return text.charAt(position);
    }

    public char la(int count) {
        return charAt(position + count);
    }

    public Sequence consume() {
        return consume(1);
    }

    public Sequence consume(int count) {
        if (!isDone()) {
            position = min(limit(), position + count);
        }
        return this;
    }

    public boolean consumeIf(char c) {
        if (curr() == c) {
            consume();
            return true;
        }
        return false;
    }

    public String consumeRemainder() {
        if (isDone()) {
            return null;
        }
        String result = text.substring(position, limit());
        position = text.length();
        return result;
    }

    public char prev() {
        if (position <= 0) {
            return 0;
        }
        return text.charAt(position - 1);
    }
    
    public void backup() {
        position = max(0, position-1);
    }
}
