package org.rivierarobotics.sharpeyes.geom;

import java.util.Collection;
import java.util.Optional;

public interface AabbCollection<E extends AabbCapable> extends Collection<E> {

    /**
     * Finds the element that intersects the point (x, y).
     *
     * @param x - the x coord
     * @param y - the y coord
     * @return the intersecting element, if it exists
     */
    Optional<E> getIntersecting(int x, int y);

}
