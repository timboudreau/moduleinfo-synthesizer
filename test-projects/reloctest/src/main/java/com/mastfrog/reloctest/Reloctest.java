package com.mastfrog.reloctest;

import com.mastfrog.reloclib.RStrings;
import com.mastfrog.reloclib.RStrings.RSInner.Thinglet;
import com.mastfrog.reloclib.RThing;
import com.mastfrog.reloclib.SomeAnno;
import com.mastfrog.reloclib.SomeInterface;
import com.mastfrog.util.service.ServiceProvider;
import notremapped.NotRemapped;

/**
 * This class is not intended to be run directly - in fact, it will fail if run
 * in target/classes - it is called by TestRelocVerificationTest, which uses the
 * JAR artifact which has had the classes relocated such that the assertions
 * here will pass.
 *
 * @author timb
 */
@SomeAnno("This is a test")
@ServiceProvider(SomeInterface.class)
public class Reloctest implements SomeInterface {

    private static final ClassNameExpectation MY_TYPE
            = type("mug-wurbles-Reloctest");
    private static final ClassNameExpectation NOT_REMAPPED
            = type("notremapped-NotRemapped");
    private static final ClassNameExpectation ANNO
            = type("urble-libs-SomeAnno");
    private static final ClassNameExpectation THINGLET
            = type("urble-libs-RStrings$RSInner$Thinglet");
    private static final ClassNameExpectation RSTRINGS
            = type("urble-libs-RStrings");

    public static void main(String[] args) throws Exception {
        MY_TYPE.assertMatch(Reloctest.class);
        RSTRINGS.assertMatch(RStrings.class);
        THINGLET.assertMatch(Thinglet.class);
        NOT_REMAPPED.assertMatch(NotRemapped.class);
        ANNO.assertMatch(SomeAnno.class);

        assertEquals("foo,bar,baz", RStrings.join(',', "foo", "bar", "baz"));

        RThing[] thingArray = RStrings.things("this", "that", "the-other");
        assertArray(thingArray, "this", "that", "the-other");

        java.util.List<RThing> thingList = RStrings.thingList("wurble", "murble", "gorg", "blarg");

        assertList(thingList, "wurble", "murble", "gorg", "blarg");

        NotRemapped not = new NotRemapped("a", "b", "c");
        NOT_REMAPPED.assertType(not);

        thingArray = not.things();

        assertArray(thingArray, "a", "b", "c");

        RStrings.RSInner.Thinglet[] lets = RStrings.thinglets("x", "y", "w is for wookie");

        assertArray(lets, "x", "y", "w is for wookie");

        RStrings.RSInner.Thinglet[][] big = RStrings.thinglets(5, "multi", "dim", "array");

        for (int i = 0; i < big.length; i++) {
            assertArray(big[i], "multi", "dim", "array");
            for (int j = 0; j < big[i].length; j++) {
                RStrings.RSInner.Thinglet x = big[i][j];
                THINGLET.assertType(x);
            }
        }

        assertEquals("I'm not remapped, but my annotation is", readAnno(NotRemapped.class));
        assertEquals("I am a thing", readAnno(RThing.class));
        assertEquals("This is a test", readAnno(Reloctest.class));

        String theTypeName = "com.mastfrog.reloclib.RStrings";

        RSTRINGS.assertIs(theTypeName);

        Class<?> c = Class.forName(theTypeName);
        if (c != RStrings.class) {
            throw new AssertionError("Not same type " + c + " and " + RStrings.class);
        }

        SomeInterface iface = java.util.ServiceLoader.load(SomeInterface.class).findFirst().get();
        assertEquals("goo!", iface.goo());
        System.out.println("All assertions passed.  Everything is awesome.");
    }

    private static void assertEquals(String a, String b) {
        if (!java.util.Objects.equals(a, b)) {
            throw new AssertionError("Mismatch: '" + a + "' and '" + b + "'");
        }
    }

    private static <T> void assertList(java.util.List<T> list, String... matching) {
        assertArray(list.toArray(Object[]::new), matching);
    }

    private static void assertArray(Object[] items, String... matching) {
        if (items.length != matching.length) {
            throw new Error("Wrong array lengths " + items.length + " and " + matching);
        }
        for (int i = 0; i < items.length; i++) {
            String s = java.util.Objects.toString(items[i]);
            assertEquals(matching[i], s);
        }
    }

    private static String readAnno(Class<?> c) {
        SomeAnno a = c.getAnnotation(SomeAnno.class);
        if (a != null) {
            return a.value();
        }
        return null;
    }

    static ClassNameExpectation type(String munged) {
        return new ClassNameExpectation(munged);
    }

    private final static class ClassNameExpectation {

        private final String name;

        ClassNameExpectation(String mungedName) {
            char[] ch = mungedName.toCharArray();
            for (int i = 0; i < ch.length; i++) {
                if (ch[i] == '-') {
                    ch[i] = '.';
                }
            }
            name = new String(ch);
        }

        void assertIs(String what) {
            assertEquals(name, what);
        }

        void assertMatch(Class<?> type) {
            assertEquals(name, type.getName());
        }

        void assertType(Object obj) {
            if (obj == null) {
                throw new AssertionError("Got null");
            }
            assertMatch(obj.getClass());
        }
    }

    @Override
    public String goo() {
        return "goo!";
    }
}
