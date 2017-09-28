package org.rivierarobotics.sharpeyes.controller;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.rivierarobotics.protos.FieldDefinition;
import org.rivierarobotics.sharpeyes.MoreBindings;
import org.rivierarobotics.sharpeyes.TitleCaseEnumConverter;

import com.google.common.eventbus.EventBus;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class FieldController {

    private static final List<FieldDefinition.Type> VALID_TYPES =
            Stream.of(FieldDefinition.Type.values())
                    .filter(t -> t != FieldDefinition.Type.UNRECOGNIZED)
                    .collect(toImmutableList());

    public final EventBus bus = new EventBus("FieldController@" + Integer.toHexString(System.identityHashCode(this)));

    private final BooleanProperty fieldValid = new SimpleBooleanProperty(true);
    // must save it to prevent GC
    @SuppressWarnings("unused")
    private BooleanExpression boundExpr;

    @FXML
    private ChoiceBox<FieldDefinition.Type> typeChoice;

    @FXML
    private TextField name;
    @FXML
    private TextField unit;
    @FXML
    private TextField weight;

    public void initialize() {
        BooleanExpression emptyName = MoreBindings.isTextBlank(name.textProperty());
        BooleanExpression selected = typeChoice.valueProperty().isNotNull();
        fieldValid.bind(boundExpr = selected.and(emptyName.not()));

        typeChoice.getItems().addAll(VALID_TYPES);
        typeChoice.setConverter(new TitleCaseEnumConverter<>(FieldDefinition.Type.class));

        weight.textProperty().addListener(obs -> {
            if (!weight.getText().isEmpty() && !validWeight(weight.getText())) {
                weight.getStyleClass().add("invalid");
            } else {
                weight.getStyleClass().remove("invalid");
            }
        });
    }

    private boolean validWeight(String text) {
        // invalid if 0 + digits
        if (text.length() > 1 && text.codePointAt(0) == '0') {
            return false;
        }
        // valid if all digits
        return text.codePoints().allMatch(cp -> '0' <= cp && cp <= '9');
    }

    public BooleanExpression fieldValidValue() {
        return fieldValid;
    }

    public void setFieldDef(FieldDefinition def) {
        typeChoice.setValue(def.getType());
        name.setText(def.getName());
        unit.setText(def.getUnit());
        weight.setText(String.valueOf(def.getWeight()));
    }

    public FieldDefinition getFieldDef() {
        FieldDefinition.Builder def = FieldDefinition.newBuilder()
                .setType(typeChoice.getValue())
                .setName(name.getText());
        String unit = this.unit.textProperty().getValueSafe().trim();
        if (!unit.isEmpty()) {
            def.setUnit(unit);
        } else {
            def.setNotHasUnit(true);
        }
        int weight = Optional.of(this.weight.textProperty().getValueSafe())
                .filter(s -> !s.isEmpty())
                .map(Integer::valueOf)
                .orElse(0);
        def.setWeight(weight);
        return def.build();
    }

}
