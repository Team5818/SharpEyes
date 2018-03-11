package org.rivierarobotics.sharpeyes.uxdrag;

import org.rivierarobotics.sharpeyes.geom.AabbCapable;

public interface UxElement<SELF extends UxElement<SELF>> extends AabbCapable {

    int getWidth();

    int getHeight();

    int getX();

    int getY();

    SELF moveTo(int x, int y);

}
