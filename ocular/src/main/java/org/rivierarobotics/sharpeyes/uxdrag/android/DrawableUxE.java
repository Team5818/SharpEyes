package org.rivierarobotics.sharpeyes.uxdrag.android;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import org.rivierarobotics.sharpeyes.uxdrag.BaseUxE;

public class DrawableUxE extends BaseUxE<DrawableUxE> implements CanvasUxE<DrawableUxE> {

    // need to hold strong reference
    private final Drawable.Callback callback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(@NonNull Drawable who) {
            dirty = true;
        }

        @Override
        public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        }

        @Override
        public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        }
    };

    private final Drawable drawable;
    private volatile boolean dirty;

    public DrawableUxE(int x, int y, int width, int height, Drawable drawable) {
        super(x, y, width, height);
        this.drawable = drawable;
        drawable.setCallback(callback);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public DrawableUxE moveTo(int x, int y) {
        return new DrawableUxE(x, y, getWidth(), getHeight(), drawable);
    }

    @Override
    public void draw(Canvas canvas, int x, int y) {
        dirty = false;
        drawable.setBounds(x, y, x + getWidth(), y + getHeight());
        drawable.draw(canvas);
    }
}
