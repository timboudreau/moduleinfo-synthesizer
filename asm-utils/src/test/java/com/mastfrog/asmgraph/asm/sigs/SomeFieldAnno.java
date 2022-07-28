package com.mastfrog.asmgraph.asm.sigs;

import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 *
 * @author timb
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface SomeFieldAnno {

    String value() default "unset";

    int intValue() default 23;

    long longValue() default 42;

    byte byteValue() default 2;

    short shortValue() default 51;

    boolean boolValue() default true;

    SomeEnum enumValue() default SomeEnum.THE_OTHER;

    InnerAnno inner() default @InnerAnno("hey");

    public @interface InnerAnno {

        String value() default "woo";
    }
}
