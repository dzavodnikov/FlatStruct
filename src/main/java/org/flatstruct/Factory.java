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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Base class for all factories.
 */
public abstract class Factory {

    private final ClassPool pool;

    /**
     * Field name to the type.
     */
    protected final Map<String, Class<?>> fieldsType = new HashMap<>();

    /**
     * Field name to the modifier.
     */
    protected final Map<String, Integer> fieldsModifiers = new HashMap<>();

    /**
     * Cache of class implementations.
     */
    private static final Map<Class<?>, Class<?>> CLASS_IMPL_CACHE = new HashMap<>();

    protected Factory(final ClassPool pool) {
        this.pool = pool;
    }

    public Factory() {
        this(ClassPool.getDefault());
    }

    protected ClassPool getClassPool() {
        return this.pool;
    }

    public abstract String getFactoryName();

    /**
     * Create name of generated class.
     *
     * @param classDef describes how to data should be located into the memory;
     * @return class name of new structure.
     */
    protected String createClassName(final Class<?> classDef) {
        return String.format("%s.%s_of_%s", classDef.getPackage().getName(), getFactoryName(),
                classDef.getSimpleName());
    }

    protected abstract void initializeFields(CtClass ctClass)
            throws CannotCompileException, NotFoundException;

    protected void verifyFieldType(final String fieldName, final Class<?> expectedType) {
        final Class<?> fieldType = this.fieldsType.get(fieldName);
        if (fieldType == null) {
            throw new RuntimeException(String.format("Field '%s' is not exists", fieldName));
        }

        if (!fieldType.equals(expectedType)) {
            throw new RuntimeException(String.format("Field '%s' has type %s instead of %s",
                    fieldName, fieldType.getName(), expectedType.getName()));
        }
    }

    protected abstract void initializeConstructor(MethodBuilder mb, String className);

    protected abstract void initializeGetter(MethodBuilder mb, Getter getterAnnotation, Class<?> returnType);

    protected abstract void initializeSetter(MethodBuilder mb, Setter setterAnnotation, Parameter param);

    protected <V> void initializeGettersAndSetters(final CtClass ctClass, final Class<?> classDef)
            throws CannotCompileException, NotFoundException {
        for (Method method : classDef.getMethods()) {
            final MethodBuilder mb = new MethodBuilder(method.getName());
            mb.addModifier("public");

            boolean wasAnnotated = false;

            for (Parameter param : method.getParameters()) {
                final Setter setterAnnotation = param.getAnnotation(Setter.class);
                if (setterAnnotation != null) {
                    wasAnnotated = true;

                    final String fieldName = setterAnnotation.value();
                    final Class<?> paramType = param.getType();
                    verifyFieldType(fieldName, paramType);

                    initializeSetter(mb, setterAnnotation, param);
                }
            }

            final Getter getterAnnotation = method.getAnnotation(Getter.class);
            if (getterAnnotation != null) {
                wasAnnotated = true;

                final String fieldName = getterAnnotation.value();
                final Class<?> returnType = method.getReturnType();
                verifyFieldType(fieldName, returnType);

                initializeGetter(mb, getterAnnotation, returnType);
            }

            if (wasAnnotated) {
                mb.addMethodTo(ctClass);
            }
        }
    }

    private String getFieldName(final java.lang.reflect.Field field) {
        try {
            final String fieldName = (String) field.get(null);
            if (fieldName == null) {
                throw new RuntimeException("Incorrect field definition: name can't be null");
            }
            if (this.fieldsType.containsKey(fieldName)) {
                throw new RuntimeException(String.format("Duplicate field name '%s'", fieldName));
            }
            return fieldName;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> getFieldType(final Field fieldMeta) {
        final Class<?> fieldType = fieldMeta.value();
        if (fieldType == null) {
            throw new RuntimeException("Incorrect field definition: type can't be null");
        }
        return fieldType;
    }

    /**
     * Generate class based on class definition.
     *
     * @param classDef  describes how to data should be located into the memory;
     * @param className name of generated class;
     * @param ctClass   created class implementation based on class definition.
     */
    private void initializeClass(final Class<?> classDef, final String className, final CtClass ctClass)
            throws NotFoundException, CannotCompileException {
        // Initialize fields.
        for (java.lang.reflect.Field field : classDef.getFields()) {
            if (field.isAnnotationPresent(Field.class)) {
                final java.lang.reflect.Type fieldDefType = field.getGenericType();
                if (fieldDefType != String.class) {
                    throw new RuntimeException("Incorrect field definition: constant should be the String");
                }

                final String fieldName = getFieldName(field);

                final Field fieldMeta = field.getAnnotation(Field.class);
                final Class<?> fieldType = getFieldType(fieldMeta);
                this.fieldsType.put(fieldName, fieldType);

                int modifiers = Modifier.PRIVATE;
                modifiers |= fieldMeta.isVolatile() ? Modifier.VOLATILE : 0;
                this.fieldsModifiers.put(fieldName, modifiers);
            }
        }

        initializeFields(ctClass);
        initializeGettersAndSetters(ctClass, classDef);
    }

    protected Class<?>[] getConstructorParameters() {
        return new Class<?>[0];
    }

    /**
     * Generate class based on class definition and create new instance of that
     * class. Wrapped by create method of custom factory.
     *
     * @param classDef describes how to data should be located into the memory;
     * @return structure instance that was generated from class definition.
     */
    protected <T> T createInstance(final Class<T> classDef, final Object... params) {
        // Verify class definition.
        if (classDef == null) {
            throw new IllegalArgumentException("Class definition parameter should not be null");
        }
        if (!classDef.isInterface()) {
            throw new IllegalArgumentException("Class definition parameter should be an interface");
        }

        // Find existing or create new implementation.
        Class<?> classImpl = CLASS_IMPL_CACHE.get(classDef);
        if (classImpl == null) {
            try {
                final String newClassName = createClassName(classDef);
                final CtClass ctClass = getClassPool().makeClass(newClassName);
                ctClass.addInterface(getClassPool().getCtClass(classDef.getName()));

                initializeClass(classDef, newClassName, ctClass);

                ctClass.detach();
                classImpl = ctClass.toClass(classDef);
                CLASS_IMPL_CACHE.put(classDef, classImpl);
            } catch (NotFoundException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }

        // Create new instance.
        try {
            return classDef.cast(classImpl.getDeclaredConstructor(getConstructorParameters()).newInstance(params));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
