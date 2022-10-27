package com.mastfrog.asmgraph.asm.model.diff;

import com.mastfrog.asmgraph.asm.model.Access;
import com.mastfrog.asmgraph.asm.model.MethodSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.info.MethodInfo;
import com.mastfrog.asmgraph.info.MethodInfo;
import com.mastfrog.util.strings.LevenshteinDistance;
import static com.mastfrog.util.strings.LevenshteinDistance.levenshteinDistance;
import com.mastfrog.util.strings.Strings;
import static com.mastfrog.util.strings.Strings.capitalize;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.util.ArrayList;
import static java.util.Collections.emptySet;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Allows for narrowing down <i>what</i> is different between two methods.
 *
 * @author Tim Boudreau
 */
public enum MethodSpecificationComponent {
    ACCESS,
    NAME,
    RETURN_TYPE,
    GENERIC_TYPES,
    THROWN_TYPES,
    ARGUMENTS;

    public String toString() {
        return capitalize(name().replace('_', ' ').toLowerCase());
    }

    public Object get(MethodInfo a) {
        switch (this) {
            case ACCESS:
                // For diffing, we don't want to generate an outsized difference
                return Integer.toString(Access.flagsFor(a.access()));
            case NAME:
                return a.name();
            case RETURN_TYPE:
                return a.signature().returnType();
            case GENERIC_TYPES:
                return a.signature().typeParameters();
            case THROWN_TYPES:
                return a.exceptions();
            case ARGUMENTS:
                return a.signature().arguments();
            default:
                throw new AssertionError(this);
        }
    }

    /**
     * Get the difference distance between two MethodInfos - when you have a set
     * of changed method signatures on both sides of a difference, this can be
     * used to make an inference about what is likely to be an updated version
     * of what.
     *
     * Uses levenshtein distance on the string representation of the value from
     * get() on each changed component, averaged.
     *
     * @param a The first info
     * @param b The second info
     * @return A distance
     */
    public static double distance(MethodInfo a, MethodInfo b) {
        Set<MethodSpecificationComponent> comps = differing(a, b);
        if (comps.isEmpty()) {
            return 0;
        }
        int compCount = comps.size();
        double sum = 0;
        for (MethodSpecificationComponent c : comps) {
            MethodSignature asig = a.signature();
            MethodSignature bsig = b.signature();
            switch (c) {
                case ARGUMENTS:
                    int oldLen = asig.arguments().size();
                    int newLen = bsig.arguments().size();
                    if (oldLen != newLen) {
                        compCount++;
                        double maxLen = max(newLen, oldLen);
                        double minLen = min(newLen, oldLen);
                        // So zero args to one is not 0/1
                        sum += maxLen - minLen;
                    }

                    sum += typeNameListDistance(asig.arguments(), bsig.arguments());
                    continue;
                case RETURN_TYPE:
                    sum += typeNameDistance(asig.returnType(), bsig.returnType());
                    continue;
                case NAME:
                    // Weight name more heavily
                    double ls = levenshteinDistance(a.name(), b.name(), false);
                    sum += ls * ls;
                    continue;
            }
            sum += LevenshteinDistance.score(c.get(a).toString(),
                    c.get(b).toString(), false);
        }
        double result = sum / compCount;
        return sum;
    }

    private static double typeNameListDistance(List<TypeName> a, List<TypeName> b) {
        if (a.isEmpty() != b.isEmpty()) {
            return 0.5;
        } else if (a.isEmpty() && b.isEmpty()) {
            return 0;
        }
        List<TypeName> sameSize = bestMatches(a, b);
        double sum = 0;
        for (int i = 0; i < a.size(); i++) {
            sum += typeNameDistance(a.get(i), sameSize.get(i));
        }
        return sum / a.size();
    }

    private static List<TypeName> bestMatches(List<TypeName> old, List<TypeName> nue) {
        List<TypeName> result = new ArrayList<>();
        for (TypeName tn : old) {
            TypeName best = bestMatch(tn, nue);
            result.add(best);
        }
        return result;
    }

    private static TypeName bestMatch(TypeName to, List<TypeName> in) {
        double bestScore = Double.MAX_VALUE;
        TypeName best = null;
        for (TypeName tn : in) {
            if (tn.equals(to)) {
                return tn;
            }
            double score = typeNameDistance(to, tn);
            if (score < bestScore) {
                best = tn;
                bestScore = score;
            }
        }
        return best;
    }

    private static double typeNameDistance(TypeName a, TypeName b) {
//        if (true) {
//            return a.equals(b) ? 0 : 1;
//        }
        return LevenshteinDistance.score(a.simpleName(), b.simpleName(), false);
    }

    /**
     * Get the set of components that differ between two MethodInfos.
     *
     * @param a The first MethodInfo
     * @param b The second MethodInfo
     * @return A set of components that differ
     */
    public static Set<MethodSpecificationComponent> differing(MethodInfo a, MethodInfo b) {
        if (a == b) {
            return emptySet();
        }
        Set<MethodSpecificationComponent> result = EnumSet.noneOf(MethodSpecificationComponent.class);
        if (!a.name().equals(b.name())) {
            result.add(NAME);
        }
        if (!a.access().equals(b.access())) {
            result.add(ACCESS);
        }
        if (!new HashSet<>(a.exceptions()).equals(new HashSet<>(b.exceptions()))) {
            result.add(THROWN_TYPES);
        }
        MethodSignature asig = a.signature();
        MethodSignature bsig = b.signature();
        if (!asig.equals(bsig)) {
            if (!asig.returnType().equals(bsig.returnType())) {
                result.add(RETURN_TYPE);
            }
            if (!asig.arguments().equals(bsig.arguments())) {
                result.add(ARGUMENTS);
            }
        }
        return result;
    }

}
