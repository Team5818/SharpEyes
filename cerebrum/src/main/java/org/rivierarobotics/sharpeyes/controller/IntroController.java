package org.rivierarobotics.sharpeyes.controller;

import java.io.File;
import java.nio.file.Path;

import org.rivierarobotics.sharpeyes.FXUtil;
import org.rivierarobotics.sharpeyes.Loader;
import org.rivierarobotics.sharpeyes.SharpEyes;
import org.rivierarobotics.sharpeyes.config.RecentlyOpened;
import org.rivierarobotics.sharpeyes.data.SourcedGame;
import org.rivierarobotics.sharpeyes.i18n.SharpEyesI18N;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleListProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class IntroController {

    private static final class ICCell extends ListCell<Path> {
        
        private final EventHandler<MouseEvent> dblClickHandler;

        ICCell(IntroController owner) {
            setContentDisplay(ContentDisplay.RIGHT);
            dblClickHandler = event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    owner.openPath(getItem());
                }
            };
            setOnMouseClicked(dblClickHandler);
        }

        @Override
        protected void updateItem(Path item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                HBox box = new HBox();
                box.setAlignment(Pos.CENTER_RIGHT);

                Label text = new Label(item.getFileName().toString());
                Button removeBtn = new Button("");
                removeBtn.setTextFill(Paint.valueOf("#ffffff"));
                removeBtn.setPadding(new Insets(1, 3, 2, 3));
                removeBtn.setFont(Loader.loadFont(10, "FontAwesome", "otf"));
                removeBtn.setBackground(new Background(new BackgroundFill(Paint.valueOf("#cc0c2f"), new CornerRadii(10), null)));

                removeBtn.setOnAction(event -> {
                    RecentlyOpened.popPath(item);
                });

                Pane spacer = new Pane();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                box.getChildren().addAll(text, spacer, removeBtn);

                setGraphic(box);
            }
        }
    }

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
    private ListView<Path> recents;

    public IntroController(Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    public void initialize() {
        title.setText(SharpEyesI18N.t("app.title.stylized"));
        version.setText(SharpEyesI18N.t("app.version"));

        recents.setItems(RecentlyOpened.getRecents());
        recents.setCellFactory(lv -> new ICCell(this));
        // hide the recents if empty
        SimpleListProperty<Path> recentsProp = new SimpleListProperty<>();
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
        openPath(chosen);
    }

    private void openPath(Path chosen) {
        ProjectController controller = new ProjectController(SourcedGame.load(chosen));
        parentWindow.hide();
        controller.display();
    }

}
