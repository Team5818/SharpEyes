package org.rivierarobotics.sharpeyes;

import java.nio.file.Path;
import java.util.Iterator;

import org.rivierarobotics.sharpeyes.controller.MenuController;
import org.rivierarobotics.sharpeyes.event.AddTabEvent;
import org.rivierarobotics.sharpeyes.event.RemoveTabEvent;

import com.google.common.eventbus.Subscribe;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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

        ScrollPane container = new ScrollPane(primaryPane);
        container.setFitToHeight(true);
        container.setFitToWidth(true);
        VBox top = new VBox(bar, container);
        VBox.setVgrow(container, Priority.ALWAYS);

        primaryStage.setScene(new Scene(top, 800, 600));
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private Tab fakeTab() {
        Tab tab = new Tab();
        tab.setStyle("visibility: false");
        return tab;
    }

    private MenuBar setupMenus(Stage primaryStage) {
        ObservableList<Path> recents = FXCollections.emptyObservableList();
        MenuController controller = new MenuController(recents, primaryStage);
        controller.bus.register(this);
        MenuBar bar = Loader.loadFxml("MainMenu", controller);
        bar.setUseSystemMenuBar(true);
        return bar;
    }

    @Subscribe
    public void addTab(AddTabEvent event) {
        Tab tab = new Tab(event.getTabName(), event.getTabContents());
        tab.setClosable(false);
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
