/*
 * This file is part of cerebrum, licensed under the MIT License (MIT).
 *
 * Copyright (c) Team5818 <https://github.com/Team5818/SharpEyes>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
