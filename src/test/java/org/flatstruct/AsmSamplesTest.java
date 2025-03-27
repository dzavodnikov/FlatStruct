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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.flatstruct.AsmSamples.Adder;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AsmSamples}.
 */
public class AsmSamplesTest {

    @Test
    void testSum() throws Exception {
        // Generate the class bytecode.
        final byte[] classBytes = AsmSamples.generateAdderClass();

        // Custom class loader to load our generated class.
        class DynamicClassLoader extends ClassLoader {
            public Class<?> defineClass(final String name, final byte[] b) {
                return defineClass(name, b, 0, b.length);
            }
        }

        final DynamicClassLoader classLoader = new DynamicClassLoader();
        final Class<?> adderClass = classLoader.defineClass("AdderImpl", classBytes);

        // Create instance and use it directly as Adder.
        final Adder adder = (Adder) adderClass.getDeclaredConstructor().newInstance();
        assertEquals(8, adder.sum(5, 3));
    }
}
