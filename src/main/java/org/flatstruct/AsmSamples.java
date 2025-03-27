package org.flatstruct;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Samples of ASM implementations.
 */
public class AsmSamples {

    // Define the interface first
    public interface Adder {
        int sum(int a, int b);
    }

    public static byte[] generateAdderClass() {
        final ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        // Define class header: Java 8, implements Adder interface.
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "AdderImpl", null, "java/lang/Object",
                new String[] { "org/flatstruct/AsmSamples$Adder" });

        // Create default constructor.
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // Create sum method implementing the interface.
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "sum", "(II)I", null, null);
        mv.visitCode();

        // Load first parameter (int a) onto stack.
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        // Load second parameter (int b) onto stack.
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        // Add the two integers.
        mv.visitInsn(Opcodes.IADD);
        // Return the result.
        mv.visitInsn(Opcodes.IRETURN);

        // Set stack size and local variables size (note: 3 because of 'this').
        mv.visitMaxs(2, 3);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}
