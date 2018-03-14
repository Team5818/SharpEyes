package org.rivierarobotics.sharpeyes;

public interface BiFunction<T, S, R> {
    R apply(T t, S s);
}
