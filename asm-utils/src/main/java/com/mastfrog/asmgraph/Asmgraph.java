package com.mastfrog.asmgraph;

import com.mastfrog.asmgraph.record.ClassRecord;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Tim Boudreau
 */
public class Asmgraph {

    public static void main(String[] args) throws IOException {
        /*
//        String pth = "/Users/timb/work/personal/mastfrog-parent/util"
//                + "/util-strings/target/classes/com/mastfrog/util"
//                + "/strings/SingleCharSequence.class";
//        String pth = "/Users/timb/work/trivial/signatures/target/classes/com/mastfrog/signatures/Signatures.class";
        String pth = "/Users/timb/work/personal/personal/asmgraph/target/test-classes/com/mastfrog/asmgraph/asm/sigs/"
                + "FakeOtherStats.class";
//                + "FakeStats.class";
//                + "Lambdae.class";
//                + "Fields.class";
//                + "Signatures.class";

        ClassProcessorListener l = new ClassProcessorListener() {
            @Override
            public void onClass(ClassRecord clazz) {
                System.out.println(clazz);
            }

            @Override
            public void onMethod(ClassRecord rec, MethodRecord method, List<? extends InvocationRecord> invocations, List<? extends FieldAccessRecord> fld,
                    List<DynamicInvocationRecord> dyns) {
                if (method.name().startsWith("lambda")) {
                    System.out.println("\n\nLAMBDA");
                    System.out.println(method);
                    System.out.println("");
                }
            }
        };

        try ( InputStream in = Files.newInputStream(Paths.get(pth), READ)) {
            l.process(in);
        }
         */

        String pth = "/Users/timb/work/personal/mastfrog-parent/util/concurrent/target/concurrent-2.8.3.jar";

        ClassProcessor l = new ClassProcessor() {
            @Override
            public void onClass(ClassRecord clazz) {
                System.out.println(clazz);
            }
        };

        l.processJar(Paths.get(pth), true);
    }
}
