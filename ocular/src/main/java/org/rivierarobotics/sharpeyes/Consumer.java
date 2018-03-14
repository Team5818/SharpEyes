package org.rivierarobotics.sharpeyes;

public interface Consumer<T> {
    void accept(T t);
}
