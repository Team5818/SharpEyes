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
import java.util.stream.Stream;

import org.rivierarobotics.sharpeyes.controller.IntroController;
import org.rivierarobotics.sharpeyes.i18n.SharpEyesI18N;

import com.google.common.io.Resources;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = applyCommonStageConfig(primaryStage);
        stage.setTitle(SharpEyesI18N.t("app.title"));

        Pane welcome = Loader.loadFxml("Intro", new IntroController(primaryStage));

        primaryStage.setScene(new Scene(welcome, 800, 600));
        addStyleSheets(stage.getScene());
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static Stage applyCommonStageConfig(Stage base) {
        base.getIcons().add(Loader.loadImage("icon"));
        return base;
    }

    public static Scene addStyleSheets(Scene scene) {
        Stream.of("CGField", "AdvancedConfig", "fonts")
                .map(s -> s + ".css")
                .map(Loader.PKG_PREFIX::concat)
                .map(Resources::getResource)
                .map(URL::toExternalForm)
                .forEach(scene.getStylesheets()::add);
        return scene;
    }

}
