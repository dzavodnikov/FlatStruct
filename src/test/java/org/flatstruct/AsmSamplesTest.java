package org.flatstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AsmSamples}.
 */
public class AsmSamplesTest {

    @Test
    void testSum() throws Exception {
        // Generate the class bytecode.
        final byte[] classBytes = AsmSamples.generateAdderClass();

        final DynamicClassLoader classLoader = new DynamicClassLoader();
        final Class<?> adderClass = classLoader.defineClass("Adder", classBytes);

        // Invoke the sum method using reflection.
        final Method sumMethod = adderClass.getMethod("sum", int.class, int.class);
        final int result = (int) sumMethod.invoke(null, 5, 3);
        assertEquals(8, result);
    }
}
