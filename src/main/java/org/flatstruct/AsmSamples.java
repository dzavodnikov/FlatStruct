package org.flatstruct;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Samples of ASM implementations.
 */
public interface AsmSamples {

    public static byte[] generateAdderClass() {
        final ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        // Define class header: Java 8, public class, named "Adder".
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "Adder", null, "java/lang/Object", null);

        // Create default constructor.
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // Create sum method that takes two integers and returns their sum.
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "sum", "(II)I", null, null);
        mv.visitCode();

        // Load first parameter (int a) onto stack.
        mv.visitVarInsn(Opcodes.ILOAD, 0);
        // Load second parameter (int b) onto stack.
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        // Add the two integers.
        mv.visitInsn(Opcodes.IADD);
        // Return the result.
        mv.visitInsn(Opcodes.IRETURN);

        // Set stack size and local variables size.
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}
