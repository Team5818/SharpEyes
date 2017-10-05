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

import static org.rivierarobotics.sharpeyes.i18n.SharpEyesI18N.ft;
import static org.rivierarobotics.sharpeyes.i18n.SharpEyesI18N.t;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.sharpeyes.FXUtil;
import org.rivierarobotics.sharpeyes.Loader;
import org.rivierarobotics.sharpeyes.SharpEyes;
import org.rivierarobotics.sharpeyes.config.RecentlyOpened;
import org.rivierarobotics.sharpeyes.data.SourcedGame;
import org.rivierarobotics.sharpeyes.event.DeleteFieldEvent;

import com.google.common.eventbus.Subscribe;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CreateGameController {

    private final ObservableList<ObservableBooleanValue> submissionReqs = FXCollections.observableArrayList();
    private final Stage parentWindow = SharpEyes.applyCommonStageConfig(new Stage(), true);

    private SourcedGame original;

    @FXML
    private Pane fields;

    @FXML
    private Button addNewField;

    @FXML
    private TextField gameName;

    @FXML
    private Button submit;

    public CreateGameController(SourcedGame game) {
        this.original = game;
    }

    public void display() {
        Parent node = Loader.loadFxml("CreateGame", this);
        if (original == null) {
            parentWindow.setTitle(t("create.game.title") + " - " + t("app.title"));
        } else {
            parentWindow.setTitle(ft("edit.game.title", original.getGame().getName()) + " - " + t("app.title"));
        }
        parentWindow.setScene(SharpEyes.addStyleSheets(new Scene(node, 800, 600)));
        parentWindow.centerOnScreen();
        parentWindow.show();
    }

    public SourcedGame getSourcedGame() {
        return original;
    }

    public void initialize() {
        BooleanBinding subReqs = setupSubmissionReqs().not();
        // store ref to prevent GC
        submit.getProperties().put("submissionReqsRef", subReqs);
        submit.disableProperty().bind(subReqs);
        submit.setOnAction(event -> onSubmit());
        submit.setDefaultButton(true);

        addNewField.setOnAction(event -> addNewField());

        if (original != null) {
            gameName.setText(original.getGame().getName());
            original.getGame().getFieldDefsList().forEach(def -> {
                FieldController controller = addNewField();
                controller.setFieldDef(def);
            });
            submit.setText("Save Game Edits");
        }
    }

    private BooleanExpression setupSubmissionReqs() {
        BooleanExpression base = gameName.textProperty().isNotEmpty();
        BooleanBinding impl = submissionReqs(base, submissionReqs);
        return impl;
    }

    private BooleanBinding submissionReqs(BooleanExpression base, ObservableList<ObservableBooleanValue> reqs) {
        return new BooleanBinding() {

            {
                bind(base);

                reqs.addListener((ListChangeListener<ObservableBooleanValue>) change -> {
                    while (change.next()) {
                        change.getAddedSubList().forEach(this::bind);
                        change.getRemoved().forEach(this::unbind);
                    }
                });
            }

            @Override
            protected boolean computeValue() {
                if (!base.get()) {
                    return false;
                }
                for (ObservableBooleanValue observableBooleanValue : reqs) {
                    if (!observableBooleanValue.get()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private FieldController addNewField() {
        FieldController controller = new FieldController();
        controller.bus.register(this);
        Node fieldForm = Loader.loadFxml("CGField", controller);
        fieldForm.setUserData(controller);
        fields.getChildren().add(fieldForm);
        submissionReqs.add(controller.fieldValidValue());
        return controller;
    }

    @Subscribe
    public void onDeleteField(DeleteFieldEvent event) {
        int index = fields.getChildren().indexOf(event.deletedField());
        if (index != -1) {
            fields.getChildren().remove(index);
            submissionReqs.remove(index);
        }
    }

    private final FileChooser saveChooser = new FileChooser();
    {
        saveChooser.getExtensionFilters().clear();
        saveChooser.getExtensionFilters().add(SharpEyes.GAME_DEFS);
        saveChooser.setTitle("Save Your Game");
    }

    private void onSubmit() {
        Game.Builder gameBuilder = Game.newBuilder();

        gameBuilder.setName(gameName.getText());

        fields.getChildren().forEach(node -> {
            FieldController fieldData = (FieldController) node.getUserData();
            gameBuilder.addFieldDefs(fieldData.getFieldDef());
        });

        Game game = gameBuilder
                .build();
        Path savePath;
        if (original == null) {
            File selected = saveChooser.showSaveDialog(parentWindow);
            if (selected == null) {
                return;
            }
            savePath = FXUtil.fixExtension(selected.toPath(), SharpEyes.GDEF_EXTENSION);
        } else {
            savePath = original.getSource();
        }

        try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(savePath))) {
            game.writeTo(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        original = SourcedGame.wrap(savePath, game);
        RecentlyOpened.pushPath(savePath);
    }

}
