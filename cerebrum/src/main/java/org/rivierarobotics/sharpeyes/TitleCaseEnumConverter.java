package org.rivierarobotics.sharpeyes;

import java.util.Locale;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import javafx.util.StringConverter;

public class TitleCaseEnumConverter<E extends Enum<E>> extends StringConverter<E> {

    private final BiMap<E, String> nameMap;

    public TitleCaseEnumConverter(Class<E> enumClass) {
        ImmutableBiMap.Builder<E, String> map = ImmutableBiMap.builder();
        for (E e : enumClass.getEnumConstants()) {
            map.put(e, name(e));
        }
        this.nameMap = map.build();
    }

    private static String name(Enum<?> object) {
        String name = object.name();
        if (name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1).toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String toString(E object) {
        return nameMap.get(object);
    }

    @Override
    public E fromString(String string) {
        return nameMap.inverse().get(string);
    }

}
