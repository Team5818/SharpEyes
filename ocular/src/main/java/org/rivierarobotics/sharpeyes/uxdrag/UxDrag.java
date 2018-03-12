package org.rivierarobotics.sharpeyes.uxdrag;

import java.util.Optional;

/**
 * UX Drag controller. Feed this drag-and-drop events.
 */
public interface UxDrag<E extends UxElement> {
    /**
     * Add an element.
     *
     * @param element - the element to add
     */
    void addElement(E element);

    /**
     * Notify controller of a drag begin event.
     *
     * @param x - the x coord
     * @param y - the y coord
     * @return the element being dragged, if present
     */
    Optional<E> beginDrag(int x, int y);

    /**
     * Notify controller of a drag end event.
     *
     * @param x       - the x coord
     * @param y       - the y coord
     * @param element - the element being dragged
     */
    void endDrag(int x, int y, E element);
}
