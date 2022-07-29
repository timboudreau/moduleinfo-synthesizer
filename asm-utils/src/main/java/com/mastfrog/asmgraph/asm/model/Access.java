package com.mastfrog.asmgraph.asm.model;

import com.mastfrog.util.strings.Strings;
import java.util.Collection;
import static java.util.Collections.emptySet;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author timb
 */
public enum Access {

    PUBLIC(0x0001),
    PRIVATE(0x0002),
    PROTECTED(0x0004),
    STATIC(0x0008),
    FINAL(0x0010),
    SYNCHRONIZED(0x0020),
    BRIDGE(0x0040),
    VARARGS(0x0080),
    NATIVE(0x0100),
    ABSTRACT(0x0400),
    STRICT(0x0800),
    SYNTHETIC(0x1000);

    private final int flags;

    Access(int flags) {
        this.flags = (short) flags;
    }

    public int flags() {
        return flags;
    }

    public boolean matches(int value) {
        return (flags & value) != 0;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static String stringFrom(Set<Access> acc) {
        return Strings.join(' ', acc);
    }

    public static String stringFrom(int val) {
        return stringFrom(from(val));
    }

    public int intValue(Collection<? extends Access> c) {
        int result = 0;
        for (Access a : c) {
            result |= a.flags();
        }
        return result;
    }

    public static Set<Access> from(int access) {
        Set<Access> result = EnumSet.noneOf(Access.class);
        for (Access a : values()) {
            if (a.matches(access)) {
                result.add(a);
            }
        }
        return result;
    }

    public static String toParseableString(Set<Access> set) {
        if (set.isEmpty()) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (Iterator<Access> it = set.iterator(); it.hasNext();) {
            Access a = it.next();
            sb.append(a.toString());
            if (it.hasNext()) {
                sb.append('-');
            }
        }
        return sb.toString();
    }

    public static Set<Access> parse(String s) {
        if ("/".equals(s)) {
            return emptySet();
        }
        Set<Access> result = EnumSet.noneOf(Access.class);
        Access[] vals = values();
        for (String part : s.split("-")) {
            for (Access a : vals) {
                if (part.equals(a.toString())) {
                    result.add(a);
                }
            }
        }
        return result;
    }
}
