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
package com.mastfrog.asmgraph.asm.model.diff;

import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.asm.model.TypeVisitor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.ArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A way to compute differences between two types or sets of method arguments,
 * to detect compatible and incompatible api changes.
 *
 * @author Tim Boudreau
 */
public final class TypeNameDifference {

    public static final TypeNameDifference EMPTY = new TypeNameDifference(emptyList(), emptyList());
    private final List<TypeNameAndDepth> afters;
    private final List<TypeNameAndDepth> befores;

    public TypeNameDifference(List<TypeNameAndDepth> as, List<TypeNameAndDepth> bs) {
        this.befores = unmodifiableList(as);
        this.afters = unmodifiableList(bs);
    }

    public static TypeNameDifference removed(TypeName nm) {
        return new TypeNameDifference(toList(nm), emptyList());
    }

    public static TypeNameDifference added(TypeName nm) {
        return new TypeNameDifference(emptyList(), toList(nm));
    }

    public boolean isEmpty() {
        return befores.isEmpty() && afters.isEmpty();
    }

    public static List<TypeNameDifference> argumentsDifference(MethodSignature a, MethodSignature b) {
        List<TypeNameDifference> result = new ArrayList<>();
        List<TypeName> aargs = a.arguments();
        List<TypeName> bargs = b.arguments();
        int max = min(aargs.size(), bargs.size());
        for (int i = 0; i < max; i++) {
            TypeName aa = aargs.get(i);
            TypeName bb = bargs.get(i);
            result.add(differences(aa, bb));
        }
        if (aargs.size() > max) {
            for (int i = max; i < aargs.size(); i++) {
                result.add(removed(aargs.get(i)));
            }
        } else if (bargs.size() > max) {
            for (int i = max; i < bargs.size(); i++) {
                result.add(added(bargs.get(i)));
            }
        }
        return result;
    }

    public List<TypeNameAndDepth> befores() {
        return befores;
    }

    public List<TypeNameAndDepth> afters() {
        return afters;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "-none-";
        }
        int max = max(afters.size(), befores.size());
        StringBuilder sb = new StringBuilder();
        if (befores.isEmpty() && !afters.isEmpty()) {
            sb.append("added ");
        } else if (!befores.isEmpty() && afters.isEmpty()) {
            sb.append("removed ");
        } else {
            sb.append("changed ");
        }
        for (int i = 0; i < max; i++) {
            String bef = i >= befores.size() ? "" : befores.get(i).toString();
            String aft = i >= afters.size() ? "" : afters.get(i).toString();
            sb.append(bef + "/" + aft);
            if (i != max - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public static TypeNameDifference differences(TypeName a, TypeName b) {
        if (a.equals(b)) {
            return EMPTY;
        }
        List<TypeNameAndDepth> as = toList(a);
        List<TypeNameAndDepth> bs = toList(b);
        Set<TypeNameAndDepth> removeFromA = new HashSet<>(bs);
        Set<TypeNameAndDepth> removeFromB = new HashSet<>(as);
        as.removeAll(removeFromA);
        bs.removeAll(removeFromB);
        return new TypeNameDifference(as, bs);
    }

    private static List<TypeNameAndDepth> toList(TypeName tn) {
        List<TypeNameAndDepth> result = new ArrayList<>();
        tn.accept((Optional<TypeName> parent, int semanticDepth, TypeVisitor.TypeNesting nesting, TypeName target, int depth) -> {
            if (target.isSemantic()) {
                result.add(new TypeNameAndDepth(semanticDepth, target));
            }
        });
        return result;
    }

    public static final class TypeNameAndDepth implements Comparable<TypeNameAndDepth> {

        public final int depth;
        public final TypeName target;

        public TypeNameAndDepth(int depth, TypeName target) {
            this.depth = depth;
            this.target = target;
        }

        @Override
        public String toString() {
            return depth + " " + target.sourceNameTruncated();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + this.depth;
            hash = 67 * hash + Objects.hashCode(this.target);
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
            final TypeNameAndDepth other = (TypeNameAndDepth) obj;
            if (this.depth != other.depth) {
                return false;
            }
            return Objects.equals(this.target, other.target);
        }

        @Override
        public int compareTo(TypeNameAndDepth o) {
            int result = Integer.compare(depth, o.depth);
            if (result == 0) {
                result = target.compareTo(o.target);
            }
            return result;
        }
    }
}
