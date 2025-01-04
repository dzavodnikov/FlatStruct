/*
 *     Copyright 2025 The FlatStruct Authors
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

import java.lang.reflect.Parameter;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

/**
 * Factory for flat structures.
 */
public class StructureFactory extends Factory {

    private final static String FACTORY_NAME = "Structure";

    public StructureFactory(final ClassPool pool) {
        super(pool);
    }

    public StructureFactory() {
        this(ClassPool.getDefault());
    }

    @Override
    public String getFactoryName() {
        return FACTORY_NAME;
    }

    @Override
    protected void initializeConstructor(final MethodBuilder mb, final String className) {
    }

    @Override
    protected void initializeFields(final CtClass ctClass) throws CannotCompileException, NotFoundException {
        for (String fieldName : this.fieldsType.keySet()) {
            final CtField ctField = new CtField(
                    getClassPool().getCtClass(this.fieldsType.get(fieldName).getName()),
                    fieldName,
                    ctClass);
            ctField.setModifiers(this.fieldsModifiers.get(fieldName));

            ctClass.addField(ctField);
        }
    }

    @Override
    protected void initializeGetter(final MethodBuilder mb, final Getter getterAnnotation, final Class<?> returnType) {
        if (getterAnnotation.isSynchronized()) {
            mb.addModifier("synchronized");
        }
        mb.setReturnType(returnType);
        mb.addBodyLine("return this.%s;", getterAnnotation.value());
    }

    @Override
    protected void initializeSetter(final MethodBuilder mb, final Setter setterAnnotation, final Parameter param) {
        String body = "this.%s = %s;";
        if (setterAnnotation.isSynchronized()) {
            body = String.format("synchronized(this) { %s }", body);
        }
        mb.addArgument(param);
        mb.addBodyLine(body, setterAnnotation.value(), param.getName());
    }

    @Override
    protected MethodBuilder initializeEquals(final String className)
            throws CannotCompileException, NotFoundException {
        final MethodBuilder mb = new MethodBuilder("equals");
        mb.addModifier("public");
        mb.addArgument(Object.class, "obj");
        mb.setReturnType(boolean.class);

        mb.addBodyLine("if (null == obj) return false;");
        mb.addBodyLine("if (this == obj) return true;");
        mb.addBodyLine("if (getClass() != obj.getClass()) return false;");

        mb.addBodyLine("%s struct = (%s) obj;", className, className);
        for (String fieldName : this.fieldsType.keySet()) {
            if (this.fieldsType.get(fieldName).isPrimitive()) {
                mb.addBodyLine("if (this.%s != struct.%s) return false;", fieldName, fieldName);
            } else {
                mb.addBodyLine("if (this.%s != null) {", fieldName);
                mb.addBodyLine("    if (!this.%s.equals(struct.%s)) return false;", fieldName, fieldName);
                mb.addBodyLine("} else {");
                mb.addBodyLine("    if (struct.%s != null) return false;", fieldName, fieldName);
                mb.addBodyLine("}");
            }
        }
        mb.addBodyLine("return true;");

        return mb;
    }

    @Override
    protected MethodBuilder initializeHashCode(final String className)
            throws CannotCompileException, NotFoundException {
        final MethodBuilder mb = new MethodBuilder("hashCode");
        mb.addModifier("public");
        mb.setReturnType(int.class);

        mb.addBodyLine("final int prime = 31;");
        mb.addBodyLine("int result = 1;");
        for (String fieldName : this.fieldsType.keySet()) {
            if (this.fieldsType.get(fieldName).isPrimitive()) {
                mb.addBodyLine("result = prime * result + (int) this.%s;", fieldName);
            } else {
                mb.addBodyLine("result = prime * result + ((this.%s == null) ? 0 : this.%s.hashCode());",
                        fieldName, fieldName);
            }
        }
        mb.addBodyLine("return result;");

        return mb;
    }

    @Override
    protected MethodBuilder initializeToString(final String className)
            throws CannotCompileException, NotFoundException {
        final MethodBuilder mb = new MethodBuilder("toString");
        mb.addModifier("public");
        mb.setReturnType(String.class);

        mb.addBodyLine("final StringBuilder sb = new StringBuilder();");
        mb.addBodyLine("sb.append(\"%s [\\n\");", className);
        for (String fieldName : this.fieldsType.keySet()) {
            mb.addBodyLine("sb.append(\"    %s=\");", fieldName);
            if (this.fieldsType.get(fieldName).isPrimitive()) {
                mb.addBodyLine("sb.append(this.%s);", fieldName);
            } else {
                mb.addBodyLine("if (this.%s != null) {", fieldName);
                mb.addBodyLine("    sb.append(\"<\");");
                mb.addBodyLine("    sb.append(this.%s == null ? 'null' : ((Object) this.%s).hashCode());",
                        fieldName, fieldName);
                mb.addBodyLine("    sb.append(\">\");");
                mb.addBodyLine("} else {");
                mb.addBodyLine("    sb.append(\"null\");");
                mb.addBodyLine("}");
            }
            mb.addBodyLine("sb.append(\"\\n\");");
        }
        mb.addBodyLine("sb.append(\"]\");");
        mb.addBodyLine("return sb.toString();");

        return mb;
    }

    public <T> T create(final Class<T> classDef) {
        return createInstance(classDef);
    }
}
