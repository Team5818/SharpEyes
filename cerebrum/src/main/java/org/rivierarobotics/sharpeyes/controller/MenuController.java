package org.rivierarobotics.sharpeyes.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.sharpeyes.Loader;
import org.rivierarobotics.sharpeyes.SharpEyes;
import org.rivierarobotics.sharpeyes.controller.CreateGameController.CGCCloseEvent;
import org.rivierarobotics.sharpeyes.data.file.FileDataProvider;
import org.rivierarobotics.sharpeyes.event.AddTabEvent;
import org.rivierarobotics.sharpeyes.event.RemoveTabEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class MenuController {

    public final EventBus bus = new EventBus("MenuController");
    private final ObservableList<Path> recentlyOpened;
    private final Window parentWindow;

    public MenuController(ObservableList<Path> recentlyOpened, Window parentWindow) {
        this.recentlyOpened = recentlyOpened;
        this.parentWindow = parentWindow;
    }

    @FXML
    private Menu openRecentGame;
    @FXML
    private MenuItem openGame;
    @FXML
    private MenuItem editGame;
    @FXML
    private MenuItem createGame;

    public void initialize() {
        recentlyOpened.addListener((Observable obs) -> rewriteRecentGames());

        openGame.setOnAction(this::openGameHandler);
        createGame.setOnAction(this::createGameHandler);
        editGame.setOnAction(this::editGameHandler);
    }

    private void rewriteRecentGames() {
        openRecentGame.getItems().clear();
        recentlyOpened.forEach(p -> {
            MenuItem newItem = new MenuItem(p.getFileName().toString());
            newItem.setOnAction(openRecentGameHandler(p));
            openRecentGame.getItems().add(newItem);
        });
    }

    private EventHandler<ActionEvent> openRecentGameHandler(Path linked) {
        return event -> {
            doOpenGame(linked);
        };
    }

    private final FileChooser chooser = new FileChooser();

    {
        chooser.getExtensionFilters().clear();
        chooser.getExtensionFilters().add(SharpEyes.GAME_DEFS);
        chooser.setTitle("Choose Your Game");
    }

    private void openGameHandler(ActionEvent event) {
        File chosen = chooser.showOpenDialog(parentWindow);
        if (chosen != null) {
            doOpenGame(chosen.toPath());
        }
    }

    private void createGameHandler(ActionEvent event) {
        CreateGameController controller = new CreateGameController(parentWindow, null, null);
        Node node = Loader.loadFxml("CreateGame", controller);
        controller.bus.register(createCloseHandler(node));
        bus.post(AddTabEvent.create("Create Game", node));
    }

    private void editGameHandler(ActionEvent event) {
        File chosen = chooser.showOpenDialog(parentWindow);
        if (chosen != null) {
            doEditGame(chosen.toPath());
        }
    }

    private void doEditGame(Path path) {
        Game game = getGame(path);
        CreateGameController controller = new CreateGameController(parentWindow, game, path);
        Node node = Loader.loadFxml("CreateGame", controller);
        controller.bus.register(createCloseHandler(node));
        bus.post(AddTabEvent.create("Edit Game: " + game.getName(), node));
    }

    private Object createCloseHandler(Node node) {
        return new Object() {

            @Subscribe
            public void onClose(CGCCloseEvent event) {
                bus.post(RemoveTabEvent.create(node));
                // open the new file
                doOpenGame(event.controller().getOriginalPath());
            }
        };
    }

    private void doOpenGame(Path path) {
        Game game = getGame(path);
        AnalyzeGameController controller = new AnalyzeGameController(new FileDataProvider(parentWindow), game);
        Node node = Loader.loadFxml("AnalyzeGame", controller);
        bus.post(AddTabEvent.create(game.getName(), node));
    }

    private Game getGame(Path path) {
        Game game;
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(path))) {
            game = Game.parseFrom(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return game;
    }

}
