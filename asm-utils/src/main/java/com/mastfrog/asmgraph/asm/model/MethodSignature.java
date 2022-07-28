package com.mastfrog.asmgraph.asm.model;

import static com.mastfrog.util.preconditions.Checks.notNull;
import java.util.ArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A parsed method signature that can be transformed, and whose toString()
 * returns the input that created it.
 *
 * @author timb
 */
public final class MethodSignature implements GenericsContext {

    private final Map<String, TypeName> typeParameters;
    private final List<TypeName> arguments;
    private final TypeName returnType;

    public MethodSignature(Map<String, TypeName> typeParameters,
            List<TypeName> arguments, TypeName returnType) {
        this.typeParameters = typeParameters == null ? emptyMap() : typeParameters;
        this.arguments = arguments == null ? emptyList() : arguments;
        this.returnType = notNull("returnType", returnType);
    }

    public String toCode() {
        StringBuilder sb = new StringBuilder();
        if (!typeParameters.isEmpty()) {
            sb.append('<');
            for (Map.Entry<String, TypeName> e : typeParameters.entrySet()) {
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                sb.append(e.getKey()).append(':');
                sb.append(e.getValue().sourceName());
            }
            sb.append('>');
        }
        sb.append(returnType.sourceName()).append(' ');
        sb.append('(');
        char curr = 'a';
        for (Iterator<TypeName> it = arguments.iterator(); it.hasNext();) {
            TypeName arg = it.next();
            sb.append(arg.sourceName());
            sb.append(' ').append((char) curr++);
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!typeParameters.isEmpty()) {
            sb.append('<');
            typeParameters.forEach((k, v) -> {
                sb.append(k).append(':').append(v.internalName());
            });
            sb.append('>');
        }
        sb.append('(');
        for (TypeName arg : arguments) {
            sb.append(arg.internalName());
        }
        sb.append(')');
        sb.append(returnType.internalName());
        return sb.toString();
    }

    public List<TypeName> arguments() {
        return unmodifiableList(arguments);
    }

    public Map<String, TypeName> typeParameters() {
        return unmodifiableMap(typeParameters);
    }

    public TypeName returnType() {
        return returnType;
    }

    public MethodSignature transform(Function<String, String> f) {
        Map<String, TypeName> newParams = typeParameters.isEmpty() ? emptyMap() : new LinkedHashMap<>(typeParameters.size());
        if (!typeParameters.isEmpty()) {
            typeParameters.forEach((k, v) -> {
                newParams.put(k, v.transform(f));
            });
        }
        List<TypeName> args = arguments.isEmpty() ? emptyList() : new ArrayList<>(arguments.size());
        if (!arguments.isEmpty()) {
            arguments.forEach(arg -> args.add(arg.transform(f)));
        }
        TypeName tn = returnType.transform(f);
        if (!tn.equals(returnType) || !arguments.equals(args) || !typeParameters.equals(newParams)) {
            return new MethodSignature(newParams, args, tn);
        }
        return this;
    }

    @Override
    public Optional<TypeName> typeOf(String name) {
        return Optional.ofNullable(this.typeParameters.get(name));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.typeParameters);
        hash = 89 * hash + Objects.hashCode(this.arguments);
        hash = 89 * hash + Objects.hashCode(this.returnType);
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
        final MethodSignature other = (MethodSignature) obj;
        if (!Objects.equals(this.typeParameters, other.typeParameters)) {
            return false;
        }
        if (!Objects.equals(this.arguments, other.arguments)) {
            return false;
        }
        return Objects.equals(this.returnType, other.returnType);
    }

}
