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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.sharpeyes.Loader;
import org.rivierarobotics.sharpeyes.SharpEyes;
import org.rivierarobotics.sharpeyes.config.ConfigManager;
import org.rivierarobotics.sharpeyes.config.RecentlyOpened;
import org.rivierarobotics.sharpeyes.controller.CreateGameController.CGCCloseEvent;
import org.rivierarobotics.sharpeyes.data.file.FileDataProvider;
import org.rivierarobotics.sharpeyes.event.AddTabEvent;
import org.rivierarobotics.sharpeyes.event.RemoveTabEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

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
    private final Window parentWindow;

    public MenuController(Window parentWindow) {
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
        RecentlyOpened.getBus().register(this);
        rewriteRecentGames();

        openGame.setOnAction(this::openGameHandler);
        createGame.setOnAction(this::createGameHandler);
        editGame.setOnAction(this::editGameHandler);
    }

    @Subscribe
    public void onRecentAdded(RecentlyOpened.Event event) {
        rewriteRecentGames();
    }

    private void rewriteRecentGames() {
        openRecentGame.getItems().clear();
        ConfigManager.loadIfNeeded().getRecentlyOpenedList().forEach(path -> {
            Path p = Paths.get(path);
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
        RecentlyOpened.pushPath(path);
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
        RecentlyOpened.pushPath(path);
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
