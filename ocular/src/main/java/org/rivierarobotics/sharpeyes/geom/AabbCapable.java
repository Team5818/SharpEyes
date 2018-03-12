package org.rivierarobotics.sharpeyes.geom;

public interface AabbCapable {

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    default int getCenterX() {
        return getX() + getWidth() / 2;
    }

    default int getCenterY() {
        return getY() + getHeight() / 2;
    }

    default boolean intersects(int x, int y) {
        return getX() <= x && x < getX() + getWidth() &&
                getY() <= y && y < getY() + getHeight();
    }

}
