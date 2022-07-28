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
package com.mastfrog.jarmerge.relocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.objectweb.asm.Type;

/**
 * Borrowed from java-vogon.
 *
 * @author Tim Boudreau
 */
final class TypeNameUtils {

    private static final Item OPEN_ANGLE = new PunctuationItem('<');
    private static final Item CLOSE_ANGLE = new PunctuationItem('>');
    private static final Item COMMA = new PunctuationItem(',');
    private static final Item SEMI = new PunctuationItem(';');
    private static final Item LPAREN = new PunctuationItem('(');
    private static final Item RPAREN = new PunctuationItem(')');
    private static final Item L = new PunctuationItem('L');
    
    private TypeNameUtils() {
        throw new AssertionError();
    }

    static class GenericTypeVisitor implements BiConsumer<Integer, String> {

        private int lastDepth;
        private final StringBuilder sb = new StringBuilder();
        private final Map<Integer,Integer> depthCounts = new HashMap<>();

        private final List<Item> items = new ArrayList<>();

        private int countForDepth(int depth) {
            return depthCounts.getOrDefault(depth, 0);
        }

        public List<Item> result() {
            done();
            return items;
        }

        GenericTypeVisitor resetDepths() {
            depthCounts.clear();
            return this;
        }

        public String rewrite(Function<String, String> rewriter) {
            done();
            StringBuilder sb = new StringBuilder();
            for (Item i : items) {
                sb.append(i.rewrite(rewriter));
            }
            return sb.toString();
        }

        private void add(int depth, String type) {
            int currCount = countForDepth(depth) + 1;
            depthCounts.put(depth, currCount);
            if (";".equals(type.trim())) {
                items.add(SEMI);
            } else {
                if (currCount - 1 > 0) {
                    items.add(COMMA);
                    sb.append(", ");
                }
                items.add(new TypeNameItem(type));
            }
            sb.append(type);
        }

        void closeDepth(int depth) {
            depthCounts.put(depth, 0);
        }

        void done() {
            while (lastDepth > 0) {
                sb.append('>');
                items.add(CLOSE_ANGLE);
                lastDepth--;
            }
        }

        @Override
        public String toString() {
            done();
            return sb.toString();
        }

        @Override
        public void accept(Integer t, String u) {
            if (t > lastDepth) {
                sb.append('<');
                items.add(OPEN_ANGLE);
            } else if (t < lastDepth) {
                for (int i = lastDepth; i > t; i--) {
                    sb.append('>');
                    items.add(CLOSE_ANGLE);
                    closeDepth(i);
                }
            }
            lastDepth = t;
            int ix = items.size();
            add(t, u);
        }
    }

    private static int closingBracketOf(int ix, String in) {
        int openCount = 0;
        for (int i = ix + 1; i < in.length(); i++) {
            if (in.charAt(i) == '<') {
                openCount++;
            }
            if (in.charAt(i) == '>' && openCount == 0) {
                return i;
            } else if (in.charAt(i) == '>') {
                openCount--;
            }
        }
        return -1;
    }

    static void visitGenericTypes(String typeName, int depth, BiConsumer<Integer, String> c) {
        typeName = typeName.trim();
        int start = typeName.indexOf('<');
        if (start < 0 && typeName.indexOf('>') >= 0) {
            throw new IllegalArgumentException("Unbalanced <>'s in " + typeName);
        }
        int end = closingBracketOf(start, typeName);
        if (start < 0 != end < 0) {
            throw new IllegalArgumentException("Unbalanced <>'s in " + typeName);
        }
        if (end < start) {
            throw new IllegalArgumentException("Not a generic "
                    + "signature - first > comes before first < in '"
                    + typeName + "'");
        }
        if (start < 0 && end < 0) {
            for (String s : typeName.split(",")) {
                s = s.trim();
                c.accept(depth, s);
            }
        } else {
            String sub = typeName.substring(start + 1, end);
            String outer = typeName.substring(0, start);
            for (String s : outer.split(",")) {
                s = s.trim();
                c.accept(depth, s);
            }
            visitGenericTypes(sub, depth + 1, c);
            if (end < typeName.length() - 1) {
                String tail = typeName.substring(end + 1, typeName.length());
                if (tail.startsWith(",")) {
                    tail = tail.substring(1).trim();
                }
                if (!tail.isEmpty()) {
                    visitGenericTypes(tail, depth, c);
                }
            }
        }
    }

    static void visitTypeNames(String typeName, Consumer<? super String> c) {
        visitGenericTypes(typeName, 0, (depth, str) -> {
            for (String item : str.trim().split("\\s+")) {
                switch (item) {
                    case "?":
                    case "extends":
                    case "super":
                    case "&":
                        continue;
                    default:
                        c.accept(item);
                }
            }
        });
    }

    interface GroupReceiver {

        void onGroup(String text, boolean parenthesized);
    }

    static int findClosingParen(String what, int at) {
        int depth = 0;
        for (int i = at + 1; i < what.length(); i++) {
            char c = what.charAt(i);
            switch (c) {
                case '(':
                    depth++;
                    break;
                case ')':
                    if (depth == 0) {
                        return i;
                    }
                    depth--;
            }
        }
        return -1;
    }

    static void visitParenthesizedGroups(String what, GroupReceiver recv) {
        visitParenthesizedGroups(what, recv, false);
    }

    static void visitParenthesizedGroups(String what, GroupReceiver recv, boolean outerInGroup) {
        int start = 0;
        for (int i = 0; i < what.length(); i++) {
            char c = what.charAt(i);
            switch (c) {
                case '(':
                    if (start < i) {
                        recv.onGroup(what.substring(start, i), outerInGroup);
                        outerInGroup = false;
                    }
                    start = i + 1;
                    if (i != what.length() - 1) {
                        int clos = findClosingParen(what, i);
                        if (clos >= 0) {
                            String sub = what.substring(i + 1, clos);
                            if (containsParenthesized(sub)) {
                                visitParenthesizedGroups(sub, recv, true);
                                start = clos + 1;
                                i = clos;
                            } else {
                                recv.onGroup(sub, true);
                                start = clos + 1;
                            }
                        }
                    }
                default:
            }
        }
        if (start < what.length() - 1) {
            recv.onGroup(what.substring(start, what.length()), outerInGroup);
        }
    }

    static boolean containsParenthesized(String what) {
        int opix = what.indexOf('(');
        int clix = opix >= 0 ? what.indexOf(')') : -1;
        return opix >= 0 && clix > opix;
    }

    static void visitInternalName(String what, GenericTypeVisitor v) {
        int opix = what.indexOf('(');
        int clix = opix >= 0 ? what.indexOf(')') : -1;
        if (opix >= 0 && clix >= 0) {
            visitParenthesizedGroups(what, (txt, paren) -> {
                if (paren) {
                    v.items.add(LPAREN);
                }
                visitGenericTypes(txt, 0, v);
                if (paren) {
                    v.items.add(RPAREN);
                }
                v.resetDepths();
            });
        } else {
            visitGenericTypes(what, 0, v);
        }
    }

    static abstract class Item {

        String rewrite(java.util.function.Function<String, String> rewriter) {
            return rewriter.apply(toString());
        }
    }

    static class PunctuationItem extends Item {

        private final char c;

        public PunctuationItem(char c) {
            this.c = c;
        }

        @Override
        public String toString() {
            return Character.toString(c);
        }

        @Override
        String rewrite(Function<String, String> rewriter) {
            return toString();
        }
    }

    private static class TypeNameItem extends Item {

        final String name;

        TypeNameItem(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Objects.hashCode(this.name);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TypeNameItem other = (TypeNameItem) obj;
            return Objects.equals(this.name, other.name);
        }
    }

    static String remapNested(String name, Function<String, String> rewriter) {
        GenericTypeVisitor gtv = new GenericTypeVisitor();
        visitInternalName(name, gtv);
        return gtv.rewrite(rewriter);
    }

    static final boolean looksLikeFQN(String what) {
        if (what.isEmpty()) {
            return false;
        }
        boolean isLeadingChar = true;
        boolean dotEncountered = false;
        for (int i = 0; i < what.length(); i++) {
            char c = what.charAt(i);
            if (Character.isWhitespace(c)) {
                return false;
            }
            if (isLeadingChar) {
                if (c == '.') {
                    return false;
                }
                if (!Character.isJavaIdentifierStart(c)) {
                    return false;
                }
                isLeadingChar = false;
            } else {
                if (c == '.') {
                    isLeadingChar = true;
                    dotEncountered = true;
                    continue;
                } else {
                    if (!Character.isJavaIdentifierPart(c)) {
                        return false;
                    }
                }
            }
        }
        return dotEncountered && !isLeadingChar;
    }
    
    public static void main(String[] args) {
        String bad = "<T:Ljava/lang/Object;>(Ljava/util/Collection<TT;>;Ljava/util/Collection<TT;>;)Z";
        String result = remapNested(bad, Function.identity());
        
        System.out.println(bad);
        System.out.println(result);
        
        Type[] types = Type.getArgumentTypes(bad);
        for (Type t : types) {
            System.out.println(" * " + t.getClassName());
        }
    }
}
