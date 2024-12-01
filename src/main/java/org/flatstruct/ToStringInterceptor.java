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

import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * Implementation of toString method.
 */
public class ToStringInterceptor {

    @RuntimeType
    public static String toString(@This Object obj) throws IllegalAccessException {
        final StringBuilder sb = new StringBuilder();

        sb.append(obj.getClass().getSimpleName());
        sb.append(" [\n");
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            final Object value = field.get(obj);
            sb.append("    ");
            sb.append(field.getName());
            sb.append("=");
            sb.append(value.toString());
            sb.append("\n");
        }
        sb.append("]");

        return sb.toString();
    }
}
