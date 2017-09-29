package org.rivierarobotics.sharpeyes.controller;

import java.util.ArrayList;
import java.util.List;

import org.rivierarobotics.protos.FieldDefinition;
import org.rivierarobotics.protos.FieldDefinition.Type;
import org.rivierarobotics.sharpeyes.common.FieldDefHelper;

import com.google.common.base.Strings;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class AdvancedConfigController {

    private final FieldDefinition.Type type;

    public AdvancedConfigController(Type type) {
        this.type = type;
    }

    private final BooleanProperty configValid = new SimpleBooleanProperty(true);

    private boolean saved = false;

    @FXML
    private TextField unit;
    @FXML
    private Parent multiplierContainer;
    @FXML
    private TextField multiplier;
    @FXML
    private Parent weightTableContainer;
    @FXML
    private GridPane weightTable;
    @FXML
    private Button save;

    private List<String> choices = new ArrayList<>();
    private List<TextField> weights = new ArrayList<>();

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public boolean isSaved() {
        return saved;
    }

    public void initialize() {
        BooleanExpression valid = new SimpleBooleanProperty(true);
        if (FieldDefHelper.isMultiplierType(type)) {
            weightTableContainer.setVisible(false);
            weightTableContainer.setManaged(false);

            valid = valid.and(checkMultiplierValidity());
        } else {
            multiplierContainer.setVisible(false);
            multiplierContainer.setManaged(false);
        }
        if (type == FieldDefinition.Type.STRING) {
            multiplierContainer.setVisible(false);
            multiplierContainer.setManaged(false);
        }

        save.disableProperty().bind(valid.not());
        configValid.bind(valid);
        save.setOnAction(event -> {
            saved = true;
            stage.close();
        });
    }

    private BooleanExpression checkMultiplierValidity() {
        BooleanExpression weightValid = Bindings.createBooleanBinding(() -> {
            String text = multiplier.getText();
            return multiplier.isDisabled() || text.isEmpty() || validWeight(text);
        }, multiplier.disabledProperty(), multiplier.textProperty());
        weightValid.addListener(obs -> {
            if (!weightValid.get()) {
                multiplier.getStyleClass().add("invalid");
            } else {
                multiplier.getStyleClass().remove("invalid");
            }
        });

        return weightValid;
    }

    private boolean validWeight(String text) {
        // invalid if 0 + digits
        if (text.length() > 1 && text.codePointAt(0) == '0') {
            return false;
        }
        // valid if all digits
        return text.codePoints().allMatch(cp -> '0' <= cp && cp <= '9');
    }

    public void setFromField(FieldDefinition def) {
        unit.setText(def.getUnit());
        if (FieldDefHelper.isMultiplierType(type)) {
            String weightText = def.getWeightsCount() == 0 ? "" : getWeightText(def.getWeights(0));
            multiplier.setText(weightText);
        } else {
            List<String> defChoices = FieldDefHelper.getChoices(def);
            List<Integer> defWeights = def.getWeightsList();
            int row = 2;
            for (int i = 0; i < defChoices.size(); i++) {
                int weight = i < defWeights.size() ? defWeights.get(i) : 0;
                Label c = new Label(defChoices.get(i));
                TextField w = new TextField(getWeightText(weight));
                GridPane.setHalignment(c, HPos.CENTER);
                GridPane.setHalignment(w, HPos.CENTER);
                weightTable.add(c, 0, row);
                weightTable.add(w, 1, row);
                row++;
                choices.add(c.getText());
                weights.add(w);
            }
        }
    }

    private static String getWeightText(int weight) {
        return weight == 0 ? "" : String.valueOf(weight);
    }

    private static int getWeight(String text) {
        return Strings.isNullOrEmpty(text) ? 0 : Integer.valueOf(text);
    }

    public void setToField(FieldDefinition.Builder def) {
        String unit = this.unit.textProperty().getValueSafe().trim();
        if (!unit.isEmpty()) {
            def.setUnit(unit);
        } else {
            def.setNotHasUnit(true);
        }
        def.clearWeights();
        if (FieldDefHelper.isMultiplierType(type)) {
            def.addWeights(getWeight(multiplier.getText()));
        } else {
            weights.stream()
                    .map(n -> getWeight(n.getText()))
                    .forEach(def::addWeights);
        }
        def.clearChoices();
        if (type == FieldDefinition.Type.CHOICE) {
            def.addAllChoices(choices);
        }
    }

    public BooleanExpression configValidValue() {
        return configValid;
    }

}
