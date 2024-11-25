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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.flatstruct.mock.Point2D;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StructureFactory}.
 */
public class StructureTest {

    @Test
    void testClassName() throws InstantiationException, IllegalAccessException {
        final StructureFactory<Point2D> factory = new StructureFactory<>();
        final Point2D point = factory.create(Point2D.class);
        assertEquals("Structure_of_Point2D", point.getClass().getSimpleName());
    }

    @Test
    void testGetSet() throws InstantiationException, IllegalAccessException {
        final StructureFactory<Point2D> factory = new StructureFactory<>();
        final Point2D point = factory.create(Point2D.class);

        assertEquals(0, point.getX());
        assertEquals(0, point.getY());

        final int x = 1;
        point.setX(x);
        assertEquals(x, point.getX());
        assertEquals(0, point.getY());

        final int y = 2;
        point.setY(y);
        assertEquals(x, point.getX());
        assertEquals(y, point.getY());
    }
}
