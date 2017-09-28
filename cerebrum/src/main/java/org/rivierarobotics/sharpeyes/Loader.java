package org.rivierarobotics.sharpeyes;

import java.io.IOException;
import java.io.UncheckedIOException;

import com.google.common.io.Resources;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

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

}
