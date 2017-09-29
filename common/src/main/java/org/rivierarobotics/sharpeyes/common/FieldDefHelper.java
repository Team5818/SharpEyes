package org.rivierarobotics.sharpeyes.common;

import java.util.List;

import org.rivierarobotics.protos.FieldDefinition;

import com.google.common.collect.ImmutableList;

public class FieldDefHelper {

    public static List<String> getChoices(FieldDefinition def) {
        return getChoices(def.getType(), def.getChoicesList());
    }

    public static List<String> getChoices(FieldDefinition.Type type, List<String> choices) {
        switch (type) {
            case BOOLEAN:
                return ImmutableList.of("false", "true");
            case CHOICE:
                return ImmutableList.copyOf(choices);
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
