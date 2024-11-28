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

/**
 * Define type of the field.
 */
public enum Type {

    BOOLEAN(boolean.class),
    BYTE(byte.class),
    CHAR(char.class),
    SHORT(short.class),
    INT(int.class),
    FLOAT(float.class),
    LONG(long.class),
    DOUBLE(double.class);

    private final Class<?> javaType;

    private Type(Class<?> javaType) {
        this.javaType = javaType;
    }

    public java.lang.reflect.Type getJavaType() {
        return this.javaType;
    }
}
