package org.flatstruct.mock;

import org.flatstruct.Getter;
import org.flatstruct.Setter;

/**
 * Testing structure.
 */
public interface Point2D {

    String X_FIELD_NAME = "x";
    String Y_FIELD_NAME = "y";

    void setX(@Setter(X_FIELD_NAME) int x);

    @Getter(X_FIELD_NAME)
    int getX();

    void setY(@Setter(Y_FIELD_NAME) int y);

    @Getter(Y_FIELD_NAME)
    int getY();
}
