package org.rivierarobotics.sharpeyes.controller;

import java.io.File;
import java.nio.file.Path;

import org.rivierarobotics.sharpeyes.FXUtil;
import org.rivierarobotics.sharpeyes.SharpEyes;
import org.rivierarobotics.sharpeyes.data.SourcedGame;
import org.rivierarobotics.sharpeyes.i18n.SharpEyesI18N;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class IntroController {

    private final Window parentWindow;

    @FXML
    private Label title;
    @FXML
    private Label version;
    @FXML
    private Button createNew;
    @FXML
    private Button openExisting;
    @FXML
    private ListView<String> recents;

    public IntroController(Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    public void initialize() {
        title.setText(SharpEyesI18N.t("app.title.stylized"));
        version.setText(SharpEyesI18N.t("app.version"));

        // hide the recents if empty
        SimpleListProperty<String> recentsProp = new SimpleListProperty<>();
        recentsProp.bind(recents.itemsProperty());
        BooleanExpression recentThere = recentsProp.emptyProperty().not();
        recents.visibleProperty().bind(recentThere);
        recents.managedProperty().bind(recentThere);

        createNew.setOnAction(event -> create());
        openExisting.setOnAction(event -> openExisting());
    }

    private void create() {
        CreateGameController controller = new CreateGameController(null);
        parentWindow.hide();
        controller.display();
    }

    private final FileChooser chooser = new FileChooser();

    {
        chooser.getExtensionFilters().clear();
        chooser.getExtensionFilters().add(SharpEyes.GAME_DEFS);
        chooser.setTitle("Choose Your Game");
    }

    private void openExisting() {
        File chosenFile = chooser.showOpenDialog(parentWindow);
        if (chosenFile == null) {
            return;
        }
        Path chosen = FXUtil.fixExtension(chosenFile.toPath(), SharpEyes.GDEF_EXTENSION);
        ProjectController controller = new ProjectController(SourcedGame.load(chosen));
        parentWindow.hide();
        controller.display();
    }

}
