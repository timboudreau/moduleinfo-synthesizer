package com.mastfrog.asmgraph;

import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.record.FieldAccessRecord;
import com.mastfrog.asmgraph.record.ClassRecord;
import com.mastfrog.asmgraph.record.DynamicInvocationRecord;
import com.mastfrog.asmgraph.record.FieldRecord;
import com.mastfrog.asmgraph.record.InvocationRecord;
import com.mastfrog.asmgraph.record.MethodRecord;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Walks a class or JAR of classes, and passes encountered fields, methods and
 * things that they call to the listener the instance was constructed with.
 *
 * @author Tim Boudreau
 */
public interface ClassProcessor {

    default void process(InputStream in) throws IOException {
        new ClassProcessorDriver(this).process(in);
    }

    default void process(Path in) throws IOException {
        new ClassProcessorDriver(this).process(in);
    }

    default void processJar(Path jar) throws IOException {
        new ClassProcessorDriver(this).processJar(jar);
    }

    default void processJar(Path jar, boolean parallel) throws IOException {
        new ClassProcessorDriver(this).processJar(jar, parallel);
    }

    /**
     * Called with details about callers and callees within each method
     * encountered.
     *
     * @param owningType The type the method was encountered on
     * @param method The method in question
     * @param invocations Methods this method invokes, with complete information
     * about their provenance
     * @param fld Fields that this method accesses, with complete information
     * about their provenance
     * @param invokeDynamics InvokeDynamic calls within this method, with enough
     * information to reconstruct what they did
     * @param referencedTypes Types referenced in other ways by this method (for
     * example, via <code>instanceof</code>).
     */
    default void onMethod(ClassRecord owningType, MethodRecord method,
            List<? extends InvocationRecord> invocations,
            List<? extends FieldAccessRecord> fld,
            List<DynamicInvocationRecord> invokeDynamics,
            Set<TypeName> referencedTypes) {
        // do nothing
    }

    /**
     * Called when a class is first encountered
     *
     * @param clazz A class
     */
    default void onClass(ClassRecord clazz) {
        // do nothing
    }

    /**
     * Called with the set of fields that belong to each class
     *
     * @param clazz A class
     * @param fields A collection of fields
     */
    default void onFields(ClassRecord clazz, List<? extends FieldRecord> fields) {
        // do nothing
    }

}
