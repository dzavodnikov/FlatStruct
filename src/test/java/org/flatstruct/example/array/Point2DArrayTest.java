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
package org.flatstruct.example.array;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.flatstruct.ArrayFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Point2DArray} and {@link ArrayFactory}.
 */
public class Point2DArrayTest {

    @Test
    void testSize() {
        final ArrayFactory arr2d = new ArrayFactory();
        final Point2DArray xyArr = arr2d.create(Point2DArray.class, 10);
        assertNotNull(xyArr);
    }
}
