package org.rivierarobotics.sharpeyes.geom;

import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;

public class AabbTree<E extends AabbCapable> extends AbstractCollection<E> implements AabbCollection<E> {

    private static final int DEFAULT_ELEMENTS_PER_NODE = 10;

    private static final class TreeNode implements AabbCapable {

        private final AabbCapable[] elements;
        private final int[] distanceCache;
        private int rawIndex = 0;
        private int x;
        private int y;
        private int width;
        private int height;
        private int size = 0;

        public TreeNode(AabbCapable[] elements) {
            this.elements = elements;
            distanceCache = new int[elements.length];
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        public Iterator<AabbCapable> iterator() {
            return new AbstractIterator<AabbCapable>() {
                private int current = 0;
                private Iterator<AabbCapable> subIterator;

                @Override
                protected AabbCapable computeNext() {
                    if (subIterator.hasNext()) {
                        return subIterator.next();
                    }
                    if (current >= rawIndex) {
                        return endOfData();
                    }
                    AabbCapable obj = elements[current];
                    current++;
                    if (obj.getClass() == TreeNode.class) {
                        subIterator = ((TreeNode) obj).iterator();
                        return computeNext();
                    }
                    return obj;
                }
            };
        }

        public AabbCapable getIntersecting(int x, int y) {
            for (AabbCapable element : elements) {
                if (element.intersects(x, y)) {
                    if (element.getClass() == TreeNode.class) {
                        return ((TreeNode) element).getIntersecting(x, y);
                    }
                    return element;
                }
            }
            return null;
        }

        public void add(AabbCapable element) {
            if (rawIndex >= elements.length) {
                addToSubNode(element);
            } else {
                elements[rawIndex] = element;
                rawIndex++;
            }
            size++;
            x = Math.min(x, element.getX());
            y = Math.min(y, element.getY());
            int maxX = Math.max(x + width, element.getX() + element.getWidth());
            int maxY = Math.max(y + height, element.getY() + element.getHeight());
            width = maxX - x;
            height = maxY - y;
        }

        private void addToSubNode(AabbCapable element) {
            TreeNode treeNode = findSubNode(element);
            treeNode.add(element);
        }

        @NonNull
        private TreeNode findSubNode(AabbCapable element) {
            // find the sub-node "closest" to element.
            // this is by center, so it is not perfect, but it is fast
            int closestIndex = 0;
            Arrays.fill(distanceCache, Integer.MIN_VALUE);
            for (int nextIndex = 1; nextIndex < elements.length; nextIndex++) {
                int closeDist = distSquared(closestIndex, element);
                int nextDist = distSquared(nextIndex, element);
                if (closeDist > nextDist) {
                    // next is closer, use it
                    closestIndex = nextIndex;
                } else if (closeDist == nextDist) {
                    // use the one with less elements
                    if (size(closestIndex) > size(nextIndex)) {
                        closestIndex = nextIndex;
                    }
                }
            }
            AabbCapable node = elements[closestIndex];
            TreeNode treeNode;
            if (node.getClass() == TreeNode.class) {
                treeNode = (TreeNode) node;
            } else {
                elements[closestIndex] = treeNode
                        = new TreeNode(new AabbCapable[elements.length]);
            }
            return treeNode;
        }

        private int size(int i) {
            AabbCapable element = elements[i];
            if (element.getClass() == TreeNode.class) {
                return ((TreeNode) element).size;
            }
            // just a regular element - size 1
            return 1;
        }

        private int distSquared(int index, AabbCapable b) {
            int val = distanceCache[index];
            if (val == Integer.MIN_VALUE) {
                AabbCapable a = elements[index];
                int x = a.getCenterX() - b.getCenterX();
                int y = a.getCenterY() - b.getCenterY();
                val = distanceCache[index]
                        = x * x + y * y;
            }
            return val;
        }
    }

    private final TreeNode root;

    public AabbTree() {
        this(DEFAULT_ELEMENTS_PER_NODE);
    }

    public AabbTree(int elementsPerNode) {
        checkArgument(elementsPerNode > 0, "must have at least 1 element per node");
        root = new TreeNode(new AabbCapable[elementsPerNode]);
    }

    @Override
    public boolean add(E e) {
        root.add(e);
        return true;
    }

    @Override
    @NonNull
    public Iterator<E> iterator() {
        @SuppressWarnings("unchecked")
        Iterator<E> iterator = (Iterator<E>) root.iterator();
        return iterator;
    }

    @Override
    public int size() {
        return root.size;
    }

    @Override
    public Optional<E> getIntersecting(int x, int y) {
        @SuppressWarnings("unchecked")
        E intersecting = (E) root.getIntersecting(x, y);
        return Optional.fromNullable(intersecting);
    }
}
