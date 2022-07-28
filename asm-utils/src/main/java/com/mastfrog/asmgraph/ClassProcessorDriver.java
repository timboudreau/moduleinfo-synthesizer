package com.mastfrog.asmgraph;

import com.mastfrog.asmgraph.asm.model.TypeName;
import com.mastfrog.asmgraph.record.FieldAccessRecord;
import com.mastfrog.asmgraph.record.InvocationRecord;
import com.mastfrog.asmgraph.record.ClassRecord;
import com.mastfrog.asmgraph.record.DynamicInvocationRecord;
import com.mastfrog.asmgraph.record.FieldRecord;
import com.mastfrog.asmgraph.record.MethodRecord;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.objectweb.asm.ClassReader;
import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ASM9;
import org.objectweb.asm.Type;

/**
 * Walks a class or JAR of classes, and passes encountered fields, methods and
 * things that they call to the listener the instance was constructed with.
 *
 * @author Tim Boudreau
 */
final class ClassProcessorDriver {

    static final ClassProcessor NONE = new ClassProcessor() {
    };
    private final ClassProcessor listener;

    ClassProcessorDriver() {
        listener = NONE;
    }

    public ClassProcessorDriver(ClassProcessor listener) {
        this.listener = listener;
    }

    public void process(InputStream in) throws IOException {
        ClassReader rdr = new ClassReader(in);
        rdr.accept(new V(), EXPAND_FRAMES);
    }

    public void process(Path path) throws IOException {
        try ( InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
            process(in);
        }
    }

    public void processJar(Path jar) throws IOException {
        processJar(jar, true);
    }

    public void processJar(Path jar, boolean parallel) throws IOException {
        JarFile jarFile = new JarFile(jar.toFile());
        List<JarEntry> list = enumToList(jarFile.entries());
        Stream<JarEntry> stream = parallel ? list.parallelStream() : list.stream();
        stream.forEach(je -> {
            if (!je.isDirectory() && je.getName().toString().endsWith(".class")) {
                try ( InputStream in = jarFile.getInputStream(je)) {
                    process(in);
                } catch (IOException ex) {
                    Logger.getLogger(ClassProcessorDriver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private static <T> List<T> enumToList(Enumeration<T> en) {
        List<T> result = new ArrayList<>();
        while (en.hasMoreElements()) {
            result.add(en.nextElement());
        }
        return result;
    }

    private void onMethod(ClassRecord rec, MethodRecord method,
            List<InvocationRecord> invocations, List<FieldAccessRecord> fld,
            List<DynamicInvocationRecord> invokeDynamics, Set<TypeName> referencedTypes) {
        listener.onMethod(rec, method, invocations, fld, invokeDynamics, referencedTypes);
    }

    private void onClass(ClassRecord rec) {
        listener.onClass(rec);
    }

    private void onFields(ClassRecord clazz, List<FieldRecord> fields) {
        listener.onFields(clazz, fields);
    }

    class V extends ClassVisitor {

        private ClassRecord curr;
        private List<FieldRecord> fields = new ArrayList<>();

        public V() {
            super(ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            ClassRecord type = new ClassRecord(version, access, name, signature, superName, interfaces);
            curr = type;
            onClass(type);
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new MV(access, name, descriptor, signature, exceptions, curr);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            FieldRecord rec = new FieldRecord(access, name, descriptor, signature, value);
            fields.add(rec);
            return null;
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(name, outerName, innerName, access);
        }

        @Override
        public void visitOuterClass(String owner, String name, String descriptor) {
            super.visitOuterClass(owner, name, descriptor);
        }

        @Override
        public void visitSource(String source, String debug) {
            super.visitSource(source, debug);
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            onFields(curr, fields);
        }
    }

    final class MV extends MethodVisitor {

        private final List<InvocationRecord> invocations = new ArrayList<>();
        private final List<FieldAccessRecord> fieldAccesses = new ArrayList<>();
        private final List<DynamicInvocationRecord> invokeDynamics = new ArrayList<>();
        private final Set<TypeName> referencedTypes = new HashSet<>();
        private final MethodRecord method;
        private final ClassRecord curr;

        private MV(int access, String name, String descriptor, String signature, String[] exceptions, ClassRecord curr) {
            super(ASM9);
            this.method = new MethodRecord(access, name, descriptor, signature, exceptions);
            this.curr = curr;
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            if ("metafactory".equals(bootstrapMethodHandle.getName())
                    && bootstrapMethodArguments.length == 3
                    && bootstrapMethodArguments[0] instanceof Type
                    && bootstrapMethodArguments[2] instanceof Type
                    && bootstrapMethodArguments[1] instanceof Handle) {
                Type type1 = (Type) bootstrapMethodArguments[0];
                Handle handle = (Handle) bootstrapMethodArguments[1];
                Type type2 = (Type) bootstrapMethodArguments[2];

                String methodCoercionTargetSignature = type1.getInternalName();
                String otherSignature = type2.getInternalName();
                String lambdaName = handle.getName();
                String lambdaOwner = handle.getOwner();
                int tag = handle.getTag();
                String methodCoercion = descriptor;
                String lambdaFunctionMethodName = name;
                DynamicInvocationRecord rec = new DynamicInvocationRecord(lambdaFunctionMethodName,
                        methodCoercion, methodCoercionTargetSignature, otherSignature,
                        lambdaName, lambdaOwner, tag, handle.isInterface());
                invokeDynamics.add(rec);
            } else {
                System.out.println("NOT METAFACTORY: " + bootstrapMethodHandle.getName());
                System.out.println(curr.name() + " " + method.name
                        + " InvD\n" + name + " " + descriptor
                        + "\n desc " + bootstrapMethodHandle.getDesc()
                        + "\n owner " + bootstrapMethodHandle.getOwner()
                        + "\n name " + bootstrapMethodHandle.getName()
                        //                    + " " + Arrays.asList(bootstrapMethodArguments)
                        + " " + bootstrapMethodArguments.length
                );
                for (int i = 0; i < bootstrapMethodArguments.length; i++) {
                    System.out.println("  " + (i + 1) + ". " + bootstrapMethodArguments[i]
                            + " (" + bootstrapMethodArguments[i].getClass().getName() + ")");
                }
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            invocations.add(new InvocationRecord(opcode, owner, name, descriptor, isInterface));
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            FieldAccessRecord far = new FieldAccessRecord(opcode, owner, name, descriptor);
            fieldAccesses.add(far);
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
            super.visitLocalVariable(name, descriptor, signature, start, end, index);
        }

        @Override
        public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
            super.visitMultiANewArrayInsn(descriptor, numDimensions);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            referencedTypes.add(TypeName.simpleName(type));
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitEnd() {
            ClassRecord rec = curr;
            onMethod(rec, method, invocations, fieldAccesses, invokeDynamics, referencedTypes);
        }
    }
}
