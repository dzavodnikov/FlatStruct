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

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;

/**
 * Factory for flat structures.
 */
public class StructureFactory<T> extends Factory<T> {

    // FIXME
    private static final String X_FIELD = "x";
    private static final String Y_FIELD = "y";

    public StructureFactory() {
        super("Structure");
    }

    @Override
    public Class<?> createImpl(final ClassLoader classLoader, final Class<T> classDef) {
        verifyClassLoader(classLoader);
        verifyClassDefinition(classDef);

        try {
            final Class<?> dynamicType = new ByteBuddy()
                    // Interface for casting.
                    .subclass(Object.class)
                    .implement(classDef)
                    .name(createClassName(classDef))
                    // Internal fields.
                    .defineField(X_FIELD, int.class, Visibility.PRIVATE)
                    .defineField(Y_FIELD, int.class, Visibility.PRIVATE)
                    // Getter for X.
                    .defineMethod("getX", int.class)
                    .intercept(FieldAccessor.ofField(X_FIELD))
                    // Setter for X.
                    .defineMethod("setX", void.class)
                    .withParameter(int.class)
                    .intercept(FieldAccessor.ofField(X_FIELD))
                    // Getter for Y.
                    .defineMethod("getY", int.class)
                    .intercept(FieldAccessor.ofField(Y_FIELD))
                    // Setter for Y.
                    .defineMethod("setY", void.class)
                    .withParameter(int.class)
                    .intercept(FieldAccessor.ofField(Y_FIELD))
                    // Create class.
                    .make()
                    .load(classLoader, ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
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
