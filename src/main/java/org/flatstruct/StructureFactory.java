/*
 *     Copyright 2009-2024 Dmitry Zavodnikov
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
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation.Composable;

/**
 * Factory for flat structures.
 */
public class StructureFactory<T> extends Factory<T> {

    public StructureFactory() {
        super("Structure");
    }

    protected <V> Builder<V> initializeFields(Builder<V> builder, final Map<String, Type> fields,
            final Class<T> classDef) {
        for (java.lang.reflect.Field field : classDef.getFields()) {
            final Field fieldMeta = field.getAnnotation(Field.class);
            if (fieldMeta != null) {
                try {
                    final java.lang.reflect.Type fieldDefType = field.getGenericType();
                    if (fieldDefType != String.class) {
                        throw new RuntimeException("Incorrect field definition: constant should be the String");
                    }

                    final String fieldName = (String) field.get(null);
                    if (fieldName == null) {
                        throw new RuntimeException("Incorrect field definition: name can't be null");
                    }
                    if (fields.containsKey(fieldName)) {
                        throw new RuntimeException(String.format("Duplicate field name '%s'", fieldName));
                    }

                    final Type fieldType = fieldMeta.value();
                    if (fieldType == null) {
                        throw new RuntimeException("Incorrect field definition: type can't be null");
                    }

                    final Visibility fieldVisibility = fieldMeta.visibility();
                    if (fieldVisibility == null) {
                        throw new RuntimeException("Incorrect field definition: visibility can't be null");
                    }

                    fields.put(fieldName, fieldType);
                    builder = builder.defineField(fieldName, fieldType.getJavaType(), fieldVisibility);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return builder;
    }

    protected <V> Builder<V> initializeGettersAndSetters(Builder<V> builder, final Map<String, Type> fields,
            final Class<T> classDef) {
        for (Method method : classDef.getMethods()) {
            final Getter getterAnnotation = method.getAnnotation(Getter.class);
            if (getterAnnotation != null) {
                final Class<?> returnType = method.getReturnType();
                final String fieldName = getterAnnotation.value();

                builder = builder.defineMethod(method.getName(), returnType)
                        .intercept(FieldAccessor.ofField(fieldName));
            }

            final List<Class<?>> fieldTypes = new ArrayList<>();
            Composable setterBody = null;
            final Parameter[] params = method.getParameters();
            for (int i = 0; i < params.length; ++i) {
                final Parameter param = params[i];

                final Setter setterAnnotation = param.getAnnotation(Setter.class);
                if (setterAnnotation != null) {
                    fieldTypes.add(param.getType());

                    final Composable fieldAccessor = FieldAccessor
                            .ofField(setterAnnotation.value())
                            .setsArgumentAt(i);
                    if (setterBody == null) {
                        setterBody = fieldAccessor;
                    } else {
                        setterBody = setterBody.andThen(fieldAccessor);
                    }
                }
            }

            if (setterBody != null) {
                builder = builder.defineMethod(method.getName(), void.class)
                        .withParameters(fieldTypes)
                        .intercept(setterBody);
            }
        }

        return builder;
    }

    @Override
    public Class<?> createImpl(final ClassLoader classLoader, final Class<T> classDef) {
        verifyClassLoader(classLoader);
        verifyClassDefinition(classDef);

        try {
            Builder<Object> builder = new ByteBuddy()
                    .subclass(Object.class)
                    .implement(classDef) // Interface for casting.
                    .name(createClassName(classDef));

            final Map<String, Type> fields = new HashMap<>();

            builder = initializeFields(builder, fields, classDef);

            builder = initializeGettersAndSetters(builder, fields, classDef);

            final Class<?> dynamicType = builder
                    .make() // Create class.
                    .load(classLoader, ClassLoadingStrategy.Default.INJECTION) // Load class.
                    .getLoaded(); // Return class.
            return dynamicType;
        } catch (IllegalArgumentException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param classDef describes how to data should be located into the memory;
     * @return structure instance that was generated from class definition.
     */
    public T create(final Class<T> classDef) {
        verifyClassDefinition(classDef);

        try {
            return classDef.cast(getImpl(classDef).getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
