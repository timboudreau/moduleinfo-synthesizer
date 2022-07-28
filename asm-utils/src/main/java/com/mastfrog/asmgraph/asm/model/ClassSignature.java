package com.mastfrog.asmgraph.asm.model;

import static com.mastfrog.util.preconditions.Checks.notNull;
import java.util.ArrayList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 * @author timb
 */
public class ClassSignature implements GenericsContext, Iterable<TypeName> {

    /*
<S:Ljava/lang/Number;:Ljava/lang/CharSequence;:Ljava/lang/Iterable<Ljava/lang/String;>;>Lcom/mastfrog/asmgraph/asm/sigs/Thing<Ljava/lang/String;Lcom/mastfrog/asmgraph/asm/sigs/Arglet;Ljava/lang/IllegalThreadStateException;>;    
     */
    private final Map<String, TypeName> typeParameters;
    private final List<TypeName> supertypes;

    public ClassSignature(Map<String, TypeName> typeParameters, List<TypeName> supertypes) {
        this.typeParameters = typeParameters == null || typeParameters.isEmpty() ? emptyMap() : typeParameters;
        this.supertypes = notNull("supertypes", supertypes);
    }

    public ClassSignature transform(Function<String, String> f) {
        List<TypeName> nue = new ArrayList<>();
        supertypes.forEach(sup -> nue.add(sup.transform(f)));
        Map<String, TypeName> ng = new LinkedHashMap<>();
        typeParameters.forEach((name, type) -> ng.put(name, type.transform(f)));
        return new ClassSignature(ng, nue);
    }

    @Override
    public Iterator<TypeName> iterator() {
        return supertypes().iterator();
    }

    public Map<String, TypeName> typeParameters() {
        return unmodifiableMap(typeParameters);
    }

    public List<TypeName> supertypes() {
        return unmodifiableList(supertypes);
    }

    @Override
    public Optional<TypeName> typeOf(String name) {
        return Optional.ofNullable(typeParameters.get(name));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!typeParameters.isEmpty()) {
            sb.append('<');
            typeParameters.forEach((name, type) -> {
                sb.append(name).append(':').append(type.internalName());
            });
            sb.append('>');
        }
        for (TypeName tn : supertypes) {
            sb.append(tn.internalName());
        }
        return sb.toString();
    }

}
