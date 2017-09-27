package org.rivierarobotics.sharpeyes;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.StringProperty;

public class MoreBindings {

    public static BooleanBinding isTextBlank(StringProperty textProperty) {
        return Bindings.createBooleanBinding(() -> textProperty.getValueSafe().trim().isEmpty(), textProperty);
    }

}
