package org.rivierarobotics.sharpeyes.uxdrag.android;

import android.graphics.Canvas;

import org.rivierarobotics.sharpeyes.uxdrag.UxElement;

public interface CanvasUxE<SELF extends CanvasUxE<SELF>> extends UxElement<SELF> {

    boolean isDirty();

    /**
     * Draw on the canvas using the given position, instead of the element's position.
     *
     * @param canvas - the canvas
     * @param x      - the x coord
     * @param y      - the y coord
     */
    void draw(Canvas canvas, int x, int y);
}
