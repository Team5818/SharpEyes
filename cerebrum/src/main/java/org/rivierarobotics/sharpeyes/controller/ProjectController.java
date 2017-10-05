package org.rivierarobotics.sharpeyes.controller;

import java.nio.file.Path;

import org.rivierarobotics.sharpeyes.Loader;
import org.rivierarobotics.sharpeyes.data.SourcedGame;
import org.rivierarobotics.sharpeyes.i18n.SharpEyesI18N;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProjectController {

    private static final class PCCell extends ListCell<Path> {

        public PCCell(ListView<Path> parent) {
            ContextMenu menu = new ContextMenu();
            MenuItem remove = new MenuItem("Remove");
            remove.setOnAction(event -> {
                parent.getItems().remove(getItem());
            });
            menu.getItems().add(remove);
            setContextMenu(menu);
        }

        @Override
        protected void updateItem(Path item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getFileName().toString());
            }
        }
    }

    private final Stage parentWindow = new Stage();
    private final SourcedGame game;

    @FXML
    private TabPane tabs;
    @FXML
    private ListView<Path> dataView;

    public ProjectController(SourcedGame game) {
        this.game = game;
    }

    public void initalize() {
        setupDataView();
    }

    private void setupDataView() {
        // step 1: try not to be so self-conscious
        dataView.setCellFactory(lv -> new PCCell(lv));
        // step 2: shift your weight into your haunches
        // step 3: give a leap into the air
        // step 4: just forget your parents are both dead!
    }

    public void display() {
        Parent node = Loader.loadFxml("Project", this);
        // inject toolbar
        VBox vbox = new VBox();
        VBox.setVgrow(node, Priority.ALWAYS);
        vbox.getChildren().addAll(MenuController.create(game), node);
        parentWindow.setScene(new Scene(vbox, 800, 600));
        parentWindow.setTitle(game.getGame().getName() + " - " + SharpEyesI18N.t("app.title"));
        parentWindow.setMaximized(true);
        parentWindow.show();
    }
}
