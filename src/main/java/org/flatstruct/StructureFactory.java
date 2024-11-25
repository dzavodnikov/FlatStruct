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
import java.util.HashSet;
import java.util.Set;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;

/**
 * Factory for flat structures.
 */
public class StructureFactory<T> extends Factory<T> {

    protected final Visibility fieldVisibility;

    public StructureFactory(final Visibility fieldVisibility) {
        super("Structure");

        this.fieldVisibility = fieldVisibility;
    }

    public StructureFactory() {
        this(Visibility.PRIVATE);
    }

    protected <V> Builder<V> addField(final Builder<V> builder, final Set<String> createdFields, final String fieldName,
            final Class<?> fieldType) {
        if (createdFields.contains(fieldName)) {
            return builder;
        }

        createdFields.add(fieldName);
        return builder.defineField(fieldName, fieldType, this.fieldVisibility);
    }

    protected <V> Builder<V> initializeClass(Builder<V> builder, final Class<T> classDef) {
        final Set<String> createdFields = new HashSet<>();

        for (Method method : classDef.getMethods()) {
            final Getter getterAnnotation = method.getAnnotation(Getter.class);
            if (getterAnnotation != null) {
                final Class<?> returnType = method.getReturnType();
                final String fieldName = getterAnnotation.value();

                builder = addField(builder, createdFields, fieldName, returnType);

                builder = builder.defineMethod(method.getName(), returnType)
                        .intercept(FieldAccessor.ofField(fieldName));
            }

            for (Parameter param : method.getParameters()) {
                final Setter setterAnnotation = param.getAnnotation(Setter.class);
                if (setterAnnotation != null) {
                    final Class<?> paramType = param.getType();
                    final String fieldName = setterAnnotation.value();

                    builder = addField(builder, createdFields, fieldName, paramType);

                    builder = builder.defineMethod(method.getName(), void.class)
                            .withParameter(paramType)
                            .intercept(FieldAccessor.ofField(fieldName));
                }
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

            builder = initializeClass(builder, classDef);

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
