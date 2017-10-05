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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.rivierarobotics.protos.Game;

import com.google.common.io.Resources;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

public class Loader {

    public static final String PKG_PREFIX = "org/rivierarobotics/";

    public static <T> T loadFxml(String resource, Object controller) {
        try {
            String normalizedName = PKG_PREFIX + resource + ".fxml";
            FXMLLoader loader = new FXMLLoader(Resources.getResource(normalizedName));
            loader.setController(controller);
            return loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Image loadImage(String resource) {
        String normalizedName = PKG_PREFIX + resource + ".png";
        return new Image(Resources.getResource(normalizedName).toString());
    }

    public static URL getI18N(String resource) {
        String normalizedName = PKG_PREFIX + "i18n/" + resource + ".lang";
        return Resources.getResource(normalizedName);
    }

    public static Game getGame(Path path) {
        Game game;
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(path))) {
            game = Game.parseFrom(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return game;
    }

    public static Font loadFont(int size, String name, String ext) {
        String normalizedName = PKG_PREFIX + "fonts/" + name + "." + ext;
        return Font.loadFont(Resources.getResource(normalizedName).toExternalForm(), size);
    }

}
