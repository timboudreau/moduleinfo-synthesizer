package com.mastfrog.asmgraph.info;

import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.util.strings.Strings;
import static java.util.Arrays.asList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 *
 * @author Tim Boudreau
 */
final class ArgNameSynthesizer implements Function<TypeName, String> {

    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(asList("abstract",
            "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else",
            "enum", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new",
            "package", "private", "protected", "public", "return", "short", "static",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while", "exports", "module", "non-sealed",
            "open", "opens", "permits", "provides", "record", "requires", "sealed",
            "to", "transitive", "uses", "var", "with", "yield", "true", "false",
            "null", "const", "goto", "strictfp"));

    private final Set<String> usedNames = new HashSet<>();
    private char ch = 'a';

    @Override
    public String apply(TypeName t) {
        String nm = t.simpleName();
        if (nm.toLowerCase().endsWith("id")) {
            nm = "id";
        }
        
        String sim = Strings.camelCaseToDelimited(nm, '/');
        int ix = sim.lastIndexOf('/');
        if (ix > 0 && ix < sim.length() - 1) {
            String result = sim.substring(ix + 1).toLowerCase();
            return nonDuplicate(result);
        }
        return nonDuplicate(sim.toLowerCase());
    }

    private String nonDuplicate(String s) {
        switch (s) {
            case "class":
                s = "type";
                break;
            default:
                if (JAVA_KEYWORDS.contains(s)) {
                    s = Character.toString(ch++);
                }
        }
        String sfx = "";
        int ct = 1;
        while (!usedNames.add(s + sfx)) {
            sfx = Integer.toString(++ct);
        }
        return s + sfx;
    }

}
