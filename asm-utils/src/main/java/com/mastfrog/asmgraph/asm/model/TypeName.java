package com.mastfrog.asmgraph.asm.model;

import com.mastfrog.asmgraph.asm.model.TypeVisitor.TypeNesting;
import com.mastfrog.function.state.Bool;
import static com.mastfrog.util.preconditions.Checks.notNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.empty;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A type name, which may wrap one or more child type names, and encapsulates
 * the JVMs internal String representation of a type (slash delimited, with
 * various prefixes and suffixes).
 *
 * @author Tim Boudreau
 */
public abstract class TypeName implements Comparable<TypeName> {

    protected TypeName() {
    }

    /**
     * Returns the internal name of this type, less any trailing delimiters such
     * as a semicolon, so that generics may be appended to the resulting string.
     *
     * @return A string
     */
    public abstract String rawName();

    /**
     * Get the representation of this type name that would be used in source
     * code, converting <code>/</code> and <code>$</code> characters to dots.
     *
     * @return A string representation of this type suitable for use in source
     * code
     */
    public abstract String sourceName();

    /**
     * Get the kind of type name this name instance represents, such as an
     * array, an object type, an object type with type parameters, etc.
     *
     * @return A kind
     */
    public abstract TypeKind kind();

    /**
     * Get the internal form of this type name, such as would be usable in a
     * method signature.
     *
     * @return
     */
    public String internalName() {
        String val = rawName();
        return notNull("rawName on " + getClass().getSimpleName(), val);
    }

    /**
     * Creates a new TypeName, using the passed function to convert the string
     * representation of this type (which should accept <code>/</code> delimited
     * internal names).
     *
     * @param f A conversion function
     * @return
     */
    public abstract TypeName transform(Function<String, String> f);

    /**
     * Given a generics context, reify this type so that type variables are
     * replaced with concrete types. Returns an empty optional if no change is
     * caused in this type.
     *
     * @param ctx A context
     * @return An optional
     */
    public Optional<TypeName> reify(GenericsContext ctx) {
        return Optional.empty();
    }

    /**
     * Determine if this type name contains any unresolved type parameter names
     * (these can be resolved with a call to <code>reify()</code> given a
     * context that contains the present type parameter names).
     *
     * @return true if type variables are present
     */
    public boolean isFullySpecified() {
        Bool paramsFound = Bool.create();
        accept((par, nest, type, depth) -> {
            paramsFound.or(type.kind() == TypeKind.TYPE_PARAMETER);
        });
        return !paramsFound.getAsBoolean();
    }

    /**
     * Visit all of the nested type names underneath this one.
     *
     * @param vis
     */
    public final void accept(TypeVisitor vis) {
        accept(empty(), TypeNesting.SELF, 0, vis);
    }

    /**
     * Get a raw TypeName, given an internal name.
     *
     * @param slashDelimitedInternalName An internal name
     * @return A TypeName
     */
    public static TypeName simpleName(String slashDelimitedInternalName) {
        return RawTypeName.of(slashDelimitedInternalName);
    }

    /**
     * Given an internal type string, get a type name suitable for use in a
     * signature.
     *
     * @param slashDelimitedInternalName A type name
     * @return A type name
     */
    public static TypeName referenceName(String slashDelimitedInternalName) {
        return new PrefixedTypeName(TypePrefix.OBJECT, simpleName(slashDelimitedInternalName));
    }

    /**
     * Internal implementation method for instances to call their children.
     * 
     * @param parent The parent, if any
     * @param nesting The nesting relationship this type has to its parent
     * @param depth The nesting depth
     * @param vis A visitor
     */
    protected final void accept(Optional<TypeName> parent, TypeNesting nesting, int depth, TypeVisitor vis) {
        vis.visit(parent, nesting, this, depth);
        visitChildren(depth + 1, vis);
    }

    /**
     * Implement this method to visit all "child" type names of this type name,
     * applying an appropriate TypeNesting indicating the reason the child is
     * nested under this parent.
     *
     * @param depth The depth at which children occur
     * @param vis A visitor
     */
    protected abstract void visitChildren(int depth, TypeVisitor vis);

    public abstract void visitTypeNames(Consumer<TypeName> c);

    /**
     * Determine if this TypeName is a raw, bottom-level type name (no leading
     * L or trailing ; to its internal name).
     * 
     * @return True if this is a raw type name
     */
    public boolean isRawTypeName() {
        return false;
    }

    /**
     * There are a few TypeName subtypes which are necessary for reconstructing
     * a signature, but do not have any semantic impact on what the type
     * expressed
     * <b>is</b> - for example raw types are wrapped in a PrefixedTypeName which
     * applies the <code>L</code> and trailing semicolon in the internal name to
     * indicate an object type, but do not fundamentally change the type (like
     * turning it from a primitive type into an array); similarly, for generic
     * type signatures, there is an InterfaceType which applies the leading
     * <code>:</code> semicolon to the internal type name, but does not alter
     * what the type expresses in any way at the source-code level.
     *
     * @return
     */
    public boolean isSemantic() {
        return true;
    }

    @Override
    public int compareTo(TypeName tn) {
        return rawName().compareTo(tn.rawName());
    }

    @Override
    public final String toString() {
        return internalName();
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || !(o instanceof TypeName)) {
            return false;
        }
        String in = ((TypeName) o).internalName();
        String mine = internalName();
        // debug
        if (in == null) {
            throw new IllegalStateException("A " + o.getClass().getSimpleName() + " returned null for internalName()");
        }
        if (mine == null) {
            throw new IllegalStateException("A " + getClass().getSimpleName() + " returned null for internalName()");
        }
        return ((TypeName) o).internalName().equals(internalName());
    }

    private int hc;

    @Override
    public final int hashCode() {
        return hc == 0 ? hc = 31 * internalName().hashCode() : hc;
    }

    /**
     * For debugging purposes, returns an indented tree view of all of the
     * TypeNames composed under this one, one TypeName per line, including type,
     * kind, semantic impact and reason for nesting.
     *
     * @return A string representation of the contents of this type name.
     */
    public final String toTreeString() {
        TreeBuildingVisitor tree = new TreeBuildingVisitor();
        accept(tree);
        return tree.toString();
    }

    private static class TreeBuildingVisitor implements TypeVisitor {

        private final StringBuilder contents = new StringBuilder();
        private final Map<Integer, String> indents = new HashMap<>();

        @Override
        public void visit(Optional<TypeName> parent, TypeNesting nesting, TypeName target, int depth) {
            onNewLine(sb -> {
                sb.append(depthString(depth));
                sb.append(target.internalName())
                        .append(' ')
                        .append(target.getClass().getSimpleName())
                        .append(' ')
                        .append(target.kind())
                        .append(' ')
                        .append(nesting)
                        .append(target.isSemantic() ? " semantic " : "")
                        .append(" @ ")
                        .append(depth);
            });
        }

        private String depthString(int depth) {
            return indents.computeIfAbsent(depth, d -> {
                char[] c = new char[d * 2];
                Arrays.fill(c, ' ');
                return new String(c);
            });
        }

        private void onNewLine(Consumer<StringBuilder> c) {
            if (contents.length() > 0 && contents.charAt(contents.length() - 1) != '\n') {
                contents.append('\n');
            }
            c.accept(contents);
        }

        public String toString() {
            return contents.toString();
        }
    }
}