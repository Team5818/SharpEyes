package org.rivierarobotics.sharpeyes.uxdrag;

import org.rivierarobotics.sharpeyes.geom.AabbCollection;
import org.rivierarobotics.sharpeyes.geom.AabbTree;

import java.util.Optional;

public class UxDragImpl<E extends UxElement<E>> implements UxDrag<E> {

    private final AabbCollection<E> elements = new AabbTree<>();

    @Override
    public void addElement(E element) {
        elements.add(element);
    }

    @Override
    public Optional<E> beginDrag(int x, int y) {
        return elements.getIntersecting(x, y);
    }

    @Override
    public void endDrag(int x, int y, E element) {
        elements.remove(element);
        addElement(element.moveTo(x, y));
    }
}
