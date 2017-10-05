package org.rivierarobotics.sharpeyes.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.rivierarobotics.sharpeyes.Loader;
import org.rivierarobotics.sharpeyes.SharpEyes;
import org.rivierarobotics.sharpeyes.data.SourcedGame;
import org.rivierarobotics.sharpeyes.fx.PathListCell;
import org.rivierarobotics.sharpeyes.i18n.SharpEyesI18N;

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
        // step 1: try not to be so self-conscious
        dataView.setCellFactory(lv -> new PCCell(lv));
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
        menu.getController().getAnalyze().setOnAction(event -> analyzeData());
        parentWindow.setScene(SharpEyes.addStyleSheets(new Scene(vbox, 800, 600)));
        parentWindow.setTitle(game.getGame().getName() + " - " + SharpEyesI18N.t("app.title"));
        parentWindow.setMaximized(true);
        parentWindow.show();
    }

    private void editGame() {
        CreateGameController ctrl = new CreateGameController(game);
        ctrl.display();
    }

    private void analyzeData() {
        AnalyzeGameController ctrl = new AnalyzeGameController(game);
        newTabYo("Analysis", Loader.loadFxml("AnalyzeGame", ctrl));
    }
}
