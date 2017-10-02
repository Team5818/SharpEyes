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
package org.rivierarobotics.sharpeyes.controller;

import java.util.List;

import org.rivierarobotics.protos.FieldDefinition;
import org.rivierarobotics.protos.FieldDefinition.Type;
import org.rivierarobotics.sharpeyes.common.FieldDefHelper;

import com.google.common.base.Strings;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdvancedConfigController {

    private static final class ACCChoiceCell extends TableCell<WeightRow, Object> {

        {
            setAlignment(Pos.CENTER);
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            if (item == getItem())
                return;

            super.updateItem(item, empty);

            if (item == null) {
                super.setText(null);
                super.setGraphic(null);
            } else if (item instanceof Node) {
                super.setText(null);
                super.setGraphic((Node) item);
            } else {
                super.setText(item.toString());
                super.setGraphic(null);
            }
        }
    }

    private static final class WeightRow {

        private final SimpleStringProperty choice = new SimpleStringProperty(this, "choice");
        private final SimpleIntegerProperty weight = new SimpleIntegerProperty(this, "weight");

        WeightRow(String choice, int weight) {
            this.choice.set(choice);
            this.weight.set(weight);
        }

    }

    private final FieldDefinition.Type type;

    public AdvancedConfigController(Type type) {
        this.type = type;
    }

    private final ObservableList<BooleanExpression> validity = FXCollections.observableArrayList();
    private final BooleanExpression configValid = new BooleanBinding() {

        {
            bind(validity);
            validity.addListener(this::bindToValidities);
        }

        private void bindToValidities(Change<? extends BooleanExpression> change) {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(this::bind);
                }
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(this::unbind);
                }
            }
        }

        @Override
        protected boolean computeValue() {
            return validity.stream()
                    .map(BooleanExpression::get)
                    .reduce(true, Boolean::logicalAnd);
        }

        @Override
        public void dispose() {
            getDependencies().forEach(this::unbind);
        }

        @Override
        public ObservableList<? extends Observable> getDependencies() {
            ObservableList<Observable> l = FXCollections.observableArrayList();
            l.add(validity);
            l.addAll(validity);
            return l;
        }
    };

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
    private Parent choiceManipContainer;
    @FXML
    private TableView<WeightRow> weightTable;
    @FXML
    private TableColumn<WeightRow, Object> choiceCol;
    @FXML
    private TableColumn<WeightRow, Object> weightCol;
    @FXML
    private Button add;
    @FXML
    private Button remove;
    @FXML
    private Button save;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public boolean isSaved() {
        return saved;
    }

    public void initialize() {
        // hide all containers
        hide(weightTableContainer);
        hide(multiplierContainer);
        hide(choiceManipContainer);
        if (FieldDefHelper.isMultiplierType(type)) {
            if (type != FieldDefinition.Type.STRING) {
                show(multiplierContainer);

                validity.add(checkMultiplierValidity(multiplier));
            }
        } else {
            show(weightTableContainer);
            if (type == FieldDefinition.Type.CHOICE) {
                show(choiceManipContainer);
                setupChoiceAdditions();
            }
            setupWeightTable();
        }

        save.disableProperty().bind(configValid.not());
        save.setOnAction(event -> {
            saved = true;
            stage.close();
        });
    }

    private void hide(Parent c) {
        c.setVisible(false);
        c.setManaged(false);
    }

    private void show(Parent c) {
        c.setVisible(true);
        c.setManaged(true);
    }

    private void setupWeightTable() {
        choiceCol.setCellFactory(col -> new ACCChoiceCell());
        choiceCol.setCellValueFactory(cdf -> {
            StringProperty bind = cdf.getValue().choice;
            if (type == FieldDefinition.Type.CHOICE) {
                TextField textField = new TextField();
                textField.textProperty().bindBidirectional(bind);
                return new SimpleObjectProperty<>(textField);
            }
            return Bindings.createObjectBinding(() -> {
                return bind.get();
            }, bind);
        });
        weightCol.setCellFactory(col -> new ACCChoiceCell());
        weightCol.setCellValueFactory(cdf -> {
            IntegerProperty weight = cdf.getValue().weight;
            TextField textField = new TextField(getWeightText(weight.get()));
            BooleanExpression checkMultiplierValidity = checkMultiplierValidity(textField);
            weight.bind(Bindings.createIntegerBinding(() -> {
                return getWeight(textField.getText());
            }, textField.textProperty()));
            validity.add(checkMultiplierValidity);
            return new SimpleObjectProperty<>(textField);
        });
    }

    private void setupChoiceAdditions() {
        add.setOnAction(event -> {
            weightTable.getItems().add(new WeightRow("", 0));
        });
        remove.setOnAction(event -> {
            int index = weightTable.getSelectionModel().getSelectedIndex();
            if (index != -1) {
                weightTable.getItems().remove(index);
                validity.remove(index);
            }
        });
    }

    private BooleanExpression checkMultiplierValidity(TextField field) {
        BooleanExpression weightValid = Bindings.createBooleanBinding(() -> {
            String text = field.getText();
            return field.isDisabled() || text.isEmpty() || validWeight(text);
        }, field.disabledProperty(), field.textProperty());
        weightValid.addListener(obs -> {
            if (!weightValid.get()) {
                field.getStyleClass().add("invalid");
            } else {
                field.getStyleClass().remove("invalid");
            }
        });

        return weightValid;
    }

    private static boolean validWeight(String text) {
        // invalid if empty
        if (Strings.isNullOrEmpty(text)) {
            return false;
        }
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
            for (int i = 0; i < defChoices.size(); i++) {
                int weight = i < defWeights.size() ? defWeights.get(i) : 0;
                weightTable.getItems().add(new WeightRow(defChoices.get(i), weight));
            }
        }
    }

    private static String getWeightText(int weight) {
        return weight == 0 ? "" : String.valueOf(weight);
    }

    private static int getWeight(String text) {
        return validWeight(text) ? Integer.valueOf(text) : 0;
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
            weightTable.getItems().stream()
                    .map(row -> row.weight.get())
                    .forEach(def::addWeights);
        }
        def.clearChoices();
        if (type == FieldDefinition.Type.CHOICE) {
            weightTable.getItems().stream()
                    .map(row -> row.choice.get())
                    .forEach(def::addChoices);
        }
    }

    public BooleanExpression configValidValue() {
        return configValid;
    }

}
