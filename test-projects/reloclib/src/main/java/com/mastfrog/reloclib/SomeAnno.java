package com.mastfrog.reloclib;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 *
 * @author timb
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface SomeAnno {

    String value();
}
