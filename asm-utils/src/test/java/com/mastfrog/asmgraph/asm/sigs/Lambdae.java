package com.mastfrog.asmgraph.asm.sigs;

import com.mastfrog.asmgraph.miniparser.CharPredicate;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author timb
 */
public class Lambdae {

    public void doSomething(Object a, String... stuff) {
        StringBuilder sb = new StringBuilder();

        List<String> x = asList(stuff);

        x.forEach(Lambdae::stringify);

        x.forEach(sb::append);

        x.forEach(str -> {
            eachChar(str, ch -> {
                System.out.println(ch);
                return true;
            });
        });

    }

    static boolean eachChar(CharSequence seq, CharPredicate p) {
        for (int i = 0; i < seq.length(); i++) {
            if (!p.test(seq.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static String stringify(Object what) {
        return Objects.toString(what);
    }
}
