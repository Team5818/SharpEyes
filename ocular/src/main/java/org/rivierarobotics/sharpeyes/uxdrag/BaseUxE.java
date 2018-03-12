package org.rivierarobotics.sharpeyes.uxdrag;

public abstract class BaseUxE<SELF extends BaseUxE<SELF>> implements UxElement<SELF> {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public BaseUxE(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
