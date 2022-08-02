package com.mastfrog.asmgraph.record;

import com.mastfrog.asmgraph.info.ClassInfo;
import com.mastfrog.asmgraph.parsing.ClassSignatureParser;
import com.mastfrog.asmgraph.asm.model.Access;
import com.mastfrog.asmgraph.asm.model.ClassSignature;
import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.miniparser.Sequence;
import com.mastfrog.util.strings.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Simply encapsulates the arguments ASM passes to a visitor on encountering a
 * class, and implements ClassInfo to provide access to parsed representations
 * of those arguments.
 *
 * @author Tim Boudreau
 */
public final class ClassRecord implements ClassInfo {

    public final int version;
    public final int access;
    public final String name;
    public final String signature;
    public final String superName;
    final String[] interfaces;

    public ClassRecord(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.version = version;
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces;
    }

    public String[] interfaceStrings() {
        return interfaces == null || interfaces.length == 0 ? new String[0]
                : Arrays.copyOf(interfaces, interfaces.length);
    }

    @Override
    public String toString() {
        return Access.stringFrom(access) + " " + name + " sig='" + signature + "' superName=" + superName + (interfaces.length == 0 ? "" : " ifaces='" + Strings.join("','", Arrays.asList(interfaces)) + "'");
    }

    @Override
    public int version() {
        return version;
    }

    @Override
    public Set<Access> access() {
        return Access.from(access);
    }

    @Override
    public TypeName name() {
        return TypeName.simpleName(name);
    }

    @Override
    public TypeName supertypeName() {
        return TypeName.simpleName(superName);
    }

    @Override
    public List<TypeName> interfaces() {
        if (interfaces.length == 0) {
            return Collections.emptyList();
        }
        List<TypeName> names = new ArrayList<>(interfaces.length);
        for (String iface : interfaces) {
            names.add(TypeName.simpleName(iface));
        }
        return names;
    }

    private String signatureString() {
        // The signature may e null, but for consistency, we should synthesize
        // one, so code can have one way of obtaining it.
        if (signature == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(toSignatureForm(name));
            if (superName != null) {
                sb.append(toSignatureForm(superName));
            }
            if (interfaces != null) {
                for (String i : interfaces) {
                    sb.append(toSignatureForm(i));
                }
            }
            return sb.toString();
        }
        return signature;
    }

    private static String toSignatureForm(String s) {
        if (s == null) {
            return "";
        }
        if (!s.endsWith(";")) {
            s = "L" + s + ";";
        }
        return s;
    }

    @Override
    public ClassSignature signature() {
        return new ClassSignatureParser().parse(new Sequence(signatureString()));
    }

}
