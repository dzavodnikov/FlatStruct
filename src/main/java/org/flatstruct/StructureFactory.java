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

    public <T> T create(final Class<T> classDef) {
        return createInstance(classDef);
    }
}
