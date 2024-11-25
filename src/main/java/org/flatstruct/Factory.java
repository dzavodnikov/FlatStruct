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

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all factories.
 */
public abstract class Factory<T> {

    private final String structName;

    protected Factory(final String structName) {
        this.structName = structName;
    }

    protected String getStructName() {
        return this.structName;
    }

    /**
     * Cache of class implementations.
     */
    private static final Map<Class<?>, Class<?>> CLASS_IMPL_CACHE = new HashMap<>();

    /**
     * @param classDef describes how to data should be located into the memory;
     * @return class name of new structure.
     */
    protected String createClassName(final Class<T> classDef) {
        return String.format("%s_of_%s", getStructName(), classDef.getSimpleName());
    }

    /**
     * @param classLoader manage created class;
     * @param classDef    describes how to data should be located into the memory;
     * @return created class implementation based on class definition.
     */
    abstract public Class<?> createImpl(ClassLoader classLoader, Class<T> classDef);

    /**
     * Create new implementation of data structure based on it's definition.
     *
     * @param classLoader manage created class;
     * @param classDef    class definition of data structure;
     * @return class implementation based on class definition.
     */
    @SuppressWarnings("unchecked")
    public Class<T> getImpl(final ClassLoader classLoader, final Class<T> classDef) {
        Class<?> classImpl = CLASS_IMPL_CACHE.get(classDef);
        if (classImpl == null) {
            classImpl = createImpl(classLoader, classDef);
            CLASS_IMPL_CACHE.put(classDef, classImpl);
        }
        return (Class<T>) classImpl;
    }

    /**
     * Create new implementation of data structure based on it's definition. Use
     * current classloader from current class.
     *
     * @param classDef class definition of data structure;
     * @return class implementation based on class definition.
     */
    public Class<T> getImpl(final Class<T> classDef) {
        return getImpl(getClass().getClassLoader(), classDef);
    }
}
