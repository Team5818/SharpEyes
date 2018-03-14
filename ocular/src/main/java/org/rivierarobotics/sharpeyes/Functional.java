package org.rivierarobotics.sharpeyes;

import com.google.common.base.Function;

import java.util.Collection;
import java.util.Comparator;

public class Functional {

    public static <T> void forEach(Collection<T> coll, Consumer<T> consumer) {
        for (T t : coll) {
            consumer.accept(t);
        }
    }

    public static <T, C extends Comparable<C>> Comparator<T> comparing(Function<T, C> key) {
        return (o1, o2) -> key.apply(o1).compareTo(key.apply(o2));
    }


    public static <T> Comparator<T> comparingInt(Function<T, Integer> key) {
        return (o1, o2) -> key.apply(o1).compareTo(key.apply(o2));
    }

}
