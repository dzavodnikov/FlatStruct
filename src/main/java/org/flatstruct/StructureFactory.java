/*
 *     Copyright 2025-2026 The FlatStruct Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flatstruct;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class StructureFactory<T> extends AsmFactory<T> {

    private final static String FACTORY_NAME = "Structure";

    @Override
    protected String getStructName() {
        return FACTORY_NAME;
    }

    @Override
    protected byte[] implementClass(final String className) {
        final ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        // Define class header: Java 8, implements interface.
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null, "java/lang/Object",
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

    @Override
    public byte[] createImpl(String className) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createImpl'");
    }
}
