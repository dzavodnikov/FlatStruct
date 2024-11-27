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
package org.flatstruct.example;

import org.flatstruct.Field;
import org.flatstruct.Getter;
import org.flatstruct.Setter;
import org.flatstruct.Type;

/**
 * Testing structure.
 */
public interface Point2D {

    @Field(Type.INT)
    String X_FIELD_NAME = "x";

    @Field(Type.INT)
    String Y_FIELD_NAME = "y";

    void setX(@Setter(X_FIELD_NAME) int x);

    @Getter(X_FIELD_NAME)
    int getX();

    void setY(@Setter(Y_FIELD_NAME) int y);

    @Getter(Y_FIELD_NAME)
    int getY();

    void setXY(@Setter(X_FIELD_NAME) int x, @Setter(Y_FIELD_NAME) int y);

    void setYX(@Setter(Y_FIELD_NAME) int y, @Setter(X_FIELD_NAME) int x);
}
