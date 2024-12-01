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

import java.lang.reflect.Field;

import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * Implementation of equals method.
 */
public class EqualsInterceptor {

    @RuntimeType
    public static boolean equals(@This Object obj, @Argument(0) Object other) throws IllegalAccessException {
        if (other == null) {
            return false;
        }

        if (obj == other) {
            return true;
        }

        if (other.getClass() != obj.getClass()) {
            return false;
        }

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            final Object value1 = field.get(obj);
            final Object value2 = field.get(other);
            if (value1 == null) {
                if (value2 != null) {
                    return false;
                }
            } else {
                if (!value1.equals(value2)) {
                    return false;
                }
            }
        }
        return true;
    }
}
