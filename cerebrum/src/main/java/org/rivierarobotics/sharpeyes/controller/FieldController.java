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

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.List;
import java.util.stream.Stream;

import org.rivierarobotics.protos.FieldDefinition;
import org.rivierarobotics.sharpeyes.Loader;
import org.rivierarobotics.sharpeyes.MoreBindings;
import org.rivierarobotics.sharpeyes.SharpEyes;
import org.rivierarobotics.sharpeyes.TitleCaseEnumConverter;

import com.google.common.eventbus.EventBus;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    private FieldDefinition activeDef = FieldDefinition.newBuilder().build();

    private SimpleBooleanProperty advancedConfigValid = new SimpleBooleanProperty(this, "advancedConfigValid", true);

    @FXML
    private ChoiceBox<FieldDefinition.Type> typeChoice;

    @FXML
    private TextField name;
    @FXML
    private Button advancedConfig;

    public void initialize() {
        BooleanExpression emptyName = MoreBindings.isTextBlank(name.textProperty());
        BooleanExpression selected = typeChoice.valueProperty().isNotNull();

        fieldValid.bind(boundExpr = selected.and(emptyName.not()).and(advancedConfigValid));

        typeChoice.getItems().addAll(VALID_TYPES);
        typeChoice.setConverter(new TitleCaseEnumConverter<>(FieldDefinition.Type.class));

        typeChoice.getSelectionModel().selectedItemProperty().addListener((o, old, n) -> {
            activeDef = activeDef.toBuilder().setType(n).build();
        });

        advancedConfig.disableProperty().bind(selected.not());
        advancedConfig.setOnAction(event -> {
            AdvancedConfigController controller = new AdvancedConfigController(typeChoice.getSelectionModel().getSelectedItem());
            Parent node = Loader.loadFxml("AdvancedConfig", controller);
            Stage popUp = new Stage();
            popUp.setResizable(false);
            popUp.setTitle("Advanced Config - " + name.getText() + " - SharpEyes");
            popUp.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(node);
            SharpEyes.addStyleSheets(scene);
            popUp.setScene(scene);
            popUp.centerOnScreen();

            controller.setStage(popUp);
            controller.setFromField(activeDef);
            popUp.showAndWait();
            advancedConfigValid.set(controller.configValidValue().get());
            if (controller.isSaved()) {
                FieldDefinition.Builder builder = activeDef.toBuilder();
                controller.setToField(builder);
                activeDef = builder.build();
            }
        });
    }

    public BooleanExpression fieldValidValue() {
        return fieldValid;
    }

    public void setFieldDef(FieldDefinition def) {
        typeChoice.setValue(def.getType());
        name.setText(def.getName());
        activeDef = def;
    }

    public FieldDefinition getFieldDef() {
        return FieldDefinition.newBuilder(activeDef)
                .setType(typeChoice.getValue())
                .setName(name.getText()).build();
    }

}
