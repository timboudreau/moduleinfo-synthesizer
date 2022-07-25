package com.mastfrog.reloclib;

import com.mastfrog.reloclib.RStrings.RSInner.Thinglet;

/**
 *
 * @author timb
 */
public class RStrings {

    public static String join(char c, Object... args) {
        StringBuilder sb = new StringBuilder();
        for (Object o : args) {
            if (sb.length() > 0) {
                sb.append(c);
            }
            sb.append(o);
        }
        return sb.toString();
    }

    public static RThing[] things(String... parts) {
        RThing[] result = new RThing[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = new RThing(parts[i]);
        }
        return result;
    }

    public static java.util.List<RThing> thingList(String... parts) {
        return java.util.Arrays.asList(things(parts));
    }

    public static Thinglet[] thinglets(String... parts) {
        Thinglet[] result = new Thinglet[parts.length];
        for (int i = 0; i < result.length; i++) {
            RSInner rs = new RSInner(parts[i]);
            result[i] = rs.thinglet();
        }
        return result;
    }

    public static Thinglet[][] thinglets(int sz, String... parts) {
        Thinglet[] lets = thinglets(parts);
        Thinglet[][] nue = new Thinglet[sz][lets.length];
        for (int i = 0; i < sz; i++) {
            nue[i] = lets;
        }
        return nue;
    }

    public static java.util.List<Thinglet> thingletList(String... parts) {
        return java.util.Arrays.asList(thinglets(parts));
    }

    public static class RSInner {

        private final String text;

        public RSInner(String text) {
            this.text = text;
        }

        Thinglet thinglet() {
            return new Thinglet();
        }

        public class Thinglet {

            @Override
            public String toString() {
                return text;
            }
        }
    }
}
