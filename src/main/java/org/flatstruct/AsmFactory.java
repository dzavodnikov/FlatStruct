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

public abstract class AsmFactory<T> extends Factory<T> {

    protected abstract byte[] implementClass(String className);

    @Override
    public Class<?> createImpl(final Class<T> classDef) {
        final DynamicClassLoader classLoader = new DynamicClassLoader();
        final String className = createClassName(classDef);
        final byte[] classBytes = implementClass(className);
        return classLoader.defineClass(className, classBytes);
    }
}
