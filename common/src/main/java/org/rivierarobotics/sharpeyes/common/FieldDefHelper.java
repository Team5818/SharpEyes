/*
 * This file is part of common, licensed under the MIT License (MIT).
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
package org.rivierarobotics.sharpeyes.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.rivierarobotics.protos.FieldDefinition;
import org.rivierarobotics.protos.FieldValue;

public class FieldDefHelper {

    public static FieldValue defaultFieldValue(FieldDefinition definition) {
        FieldValue.Builder builder = FieldValue.newBuilder();
        switch (definition.getType()) {
            case INTEGER:
                builder.setInteger(0L);
                break;
            case FLOATING:
                builder.setFloating(0.0f);
                break;
            case BOOLEAN:
                builder.setBoole(false);
                break;
            case STRING:
                builder.setStr("");
                break;
            case CHOICE:
                builder.setStr(definition.getChoices(0));
                break;
            default:
                throw new IllegalStateException("Unknown field def type: " + definition.getType().name());
        }
        return builder.build();
    }

    public static List<String> getChoices(FieldDefinition def) {
        return getChoices(def.getType(), def.getChoicesList());
    }

    public static List<String> getChoices(FieldDefinition.Type type, List<String> choices) {
        switch (type) {
            case BOOLEAN:
                return Collections.unmodifiableList(Arrays.asList("false", "true"));
            case CHOICE:
                return Collections.unmodifiableList(new ArrayList<>(choices));
            default:
                throw new IllegalArgumentException("Not a choice-based field.");
        }
    }

    public static boolean isMultiplierType(FieldDefinition.Type type) {
        switch (type) {
            case INTEGER:
            case FLOATING:
            case STRING:
                return true;
            default:
                return false;
        }
    }

}
