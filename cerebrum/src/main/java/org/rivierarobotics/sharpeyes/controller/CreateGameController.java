package org.rivierarobotics.sharpeyes.controller;

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
import org.rivierarobotics.sharpeyes.event.DeleteFieldEvent;

import com.google.auto.value.AutoValue;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class CreateGameController {

    @AutoValue
    public abstract static class CGCCloseEvent {

        public static CGCCloseEvent create(CreateGameController controller) {
            return new AutoValue_CreateGameController_CGCCloseEvent(controller);
        }

        CGCCloseEvent() {
        }

        public abstract CreateGameController controller();

    }

    public final EventBus bus = new EventBus("CreateGame");

    private final ObservableList<ObservableBooleanValue> submissionReqs = FXCollections.observableArrayList();
    private final Window parentWindow;

    private final Game original;
    private Path originalPath;

    @FXML
    private Pane fields;

    @FXML
    private Control addNewFieldLabelButton;

    @FXML
    private TextField gameName;

    @FXML
    private Button submit;

    public CreateGameController(Window parentWindow, Game original, Path originalPath) {
        this.parentWindow = parentWindow;
        this.original = original;
        this.originalPath = originalPath;
    }

    public Path getOriginalPath() {
        return originalPath;
    }

    public void initialize() {
        BooleanBinding subReqs = setupSubmissionReqs().not();
        // store ref to prevent GC
        submit.getProperties().put("submissionReqsRef", subReqs);
        submit.disableProperty().bind(subReqs);
        submit.setOnAction(event -> onSubmit());

        addNewFieldLabelButton.setOnMouseClicked(event -> addNewField());

        if (original != null) {
            gameName.setText(original.getName());
            original.getFieldDefsList().forEach(def -> {
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
        Path p = originalPath;
        if (p == null) {
            File selected = saveChooser.showSaveDialog(parentWindow);
            if (selected == null) {
                return;
            }
            p = FXUtil.fixExtension(selected.toPath(), SharpEyes.GDEF_EXTENSION);
        }

        try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(p))) {
            game.writeTo(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        originalPath = p;
        bus.post(CGCCloseEvent.create(this));
    }

}
