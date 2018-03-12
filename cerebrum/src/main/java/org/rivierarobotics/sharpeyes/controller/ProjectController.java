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

import org.rivierarobotics.sharpeyes.Loader;
import org.rivierarobotics.sharpeyes.SharpEyes;
import org.rivierarobotics.sharpeyes.data.DataProvider;
import org.rivierarobotics.sharpeyes.data.SourcedGame;
import org.rivierarobotics.sharpeyes.fx.DataProviderSelectorDialog;
import org.rivierarobotics.sharpeyes.fx.PathListCell;
import org.rivierarobotics.sharpeyes.i18n.SharpEyesI18N;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProjectController {

    private static final class PCCell extends PathListCell {

        public PCCell(ListView<Path> parent) {
            ContextMenu menu = new ContextMenu();
            MenuItem remove = new MenuItem("Remove");
            remove.setOnAction(event -> {
                parent.getItems().remove(getItem());
            });
            menu.getItems().add(remove);
            setContextMenu(menu);
        }

    }

    private final Stage parentWindow = SharpEyes.applyCommonStageConfig(new Stage(), true);
    private final SourcedGame game;

    @FXML
    private TabPane tabs;
    @FXML
    private ListView<Path> dataView;

    public ProjectController(SourcedGame game) {
        this.game = game;
    }

    private void newTabYo(String name, Parent content) {
        Tab tab = new Tab(name, content);
        tabs.getTabs().add(tabs.getTabs().size() - 1, tab);
        tabs.getSelectionModel().select(tab);
    }

    public void initialize() {
        setupDataView();
    }

    private void setupDataView() {
        dataView.setCellFactory(PCCell::new);
        reloadFilesList();
    }

    private void reloadFilesList() {
        dataView.getItems().clear();
        // step 1: try not to be so self-conscious
        // step 2: shift your weight into your haunches
        try (Stream<Path> projectFiles = Files.list(game.getSource().getParent())) {
            // step 3: give a leap into the air
            projectFiles
                    .filter(p -> p.toString().endsWith("." + SharpEyes.FRTSM_EXTENSION))
                    .forEach(dataView.getItems()::add);
        } catch (IOException e) {
            // step 4: just forget your parents are both dead!
            throw new UncheckedIOException(e);
        }
    }

    public void display() {
        Parent node = Loader.loadFxml("Project", this);
        // inject toolbar
        VBox vbox = new VBox();
        VBox.setVgrow(node, Priority.ALWAYS);
        ControlledNode<MenuController, ToolBar> menu = MenuController.create();
        vbox.getChildren().addAll(menu.getNode(), node);
        menu.getController().getEdit().setOnAction(event -> editGame());
        menu.getController().getGetData().setOnAction(event -> getData());
        menu.getController().getAnalyze().setOnAction(event -> analyzeData());
        parentWindow.setScene(SharpEyes.addStyleSheets(new Scene(vbox, 800, 600)));
        parentWindow.setTitle(game.getGame().getCurrentInstance().getName() + " - " + SharpEyesI18N.t("app.title"));
        parentWindow.setMaximized(true);
        parentWindow.show();
    }

    private void editGame() {
        CreateGameController ctrl = new CreateGameController(game);
        ctrl.display();
    }

    private void getData() {
        Optional<DataProvider> provider = DataProviderSelectorDialog.create().show(parentWindow);
        if (!provider.isPresent()) {
            return;
        }
        provider.get().provideMatches().thenAccept(imported -> {
            Platform.runLater(() -> {
                reloadFilesList();
            });
        });
    }

    private void analyzeData() {
        AnalyzeGameController ctrl = new AnalyzeGameController(game);
        newTabYo("Analysis", Loader.loadFxml("AnalyzeGame", ctrl));
    }
}
