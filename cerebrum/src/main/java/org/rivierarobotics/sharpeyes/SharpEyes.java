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
package org.rivierarobotics.sharpeyes;

import java.net.URL;
import java.util.Iterator;
import java.util.stream.Stream;

import org.rivierarobotics.sharpeyes.controller.MenuController;
import org.rivierarobotics.sharpeyes.event.AddTabEvent;
import org.rivierarobotics.sharpeyes.event.RemoveTabEvent;

import com.google.common.eventbus.Subscribe;
import com.google.common.io.Resources;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class SharpEyes extends Application {

    public static final String GDEF_EXTENSION = "gmdef";
    public static final ExtensionFilter GAME_DEFS = new ExtensionFilter("Game Definition Files", "*." + GDEF_EXTENSION);

    public static final String FRTSM_EXTENSION = "frtsm";
    public static final ExtensionFilter FRAME_TRANSMISSIONS = new ExtensionFilter("Frame Transmission Files", "*." + FRTSM_EXTENSION);

    public static void main(String[] args) {
        Application.launch(args);
    }

    private Stage stage;
    private TabPane primaryPane;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        stage.setTitle("Sharp Eyes");
        stage.getIcons().add(Loader.loadImage("icon"));
        MenuBar bar = setupMenus(primaryStage);

        primaryPane = new TabPane();
        // add a fake tab to get the header to always show up
        primaryPane.getTabs().add(fakeTab());
        primaryPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);

        ScrollPane container = new ScrollPane(primaryPane);
        container.setFitToHeight(true);
        container.setFitToWidth(true);
        VBox top = new VBox(bar, container);
        VBox.setVgrow(container, Priority.ALWAYS);

        primaryStage.setScene(new Scene(top, 800, 600));
        addStyleSheets(stage.getScene());
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void addStyleSheets(Scene scene) {
        Stream.of("CGField", "AdvancedConfig")
                .map(s -> s + ".css")
                .map(Loader.PKG_PREFIX::concat)
                .map(Resources::getResource)
                .map(URL::toString)
                .forEach(scene.getStylesheets()::add);
    }

    private Tab fakeTab() {
        Tab tab = new Tab();
        tab.setStyle("visibility: false");
        return tab;
    }

    private MenuBar setupMenus(Stage primaryStage) {
        MenuController controller = new MenuController(primaryStage);
        controller.bus.register(this);
        MenuBar bar = Loader.loadFxml("MainMenu", controller);
        bar.setUseSystemMenuBar(true);
        return bar;
    }

    @Subscribe
    public void addTab(AddTabEvent event) {
        Tab tab = new Tab(event.getTabName(), event.getTabContents());
        ObservableList<Tab> tabs = primaryPane.getTabs();
        // insert it before the hidden fake-tab
        tabs.add(tabs.size() - 1, tab);
        // focus it
        primaryPane.getSelectionModel().select(tab);
    }

    @Subscribe
    public void removeTab(RemoveTabEvent event) {
        for (Iterator<Tab> iter = primaryPane.getTabs().iterator(); iter.hasNext();) {
            Tab next = iter.next();
            if (next.getContent() == event.getTabContents()) {
                iter.remove();
                return;
            }
        }
    }

}
