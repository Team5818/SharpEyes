package org.rivierarobotics.sharpeyes;

import java.nio.file.Path;

public class FXUtil {

    public static Path fixExtension(Path selected, String ext) {
        if (!selected.toString().endsWith(ext)) {
            return selected.resolveSibling(selected.getFileName().toString() + "." + ext);
        }
        return selected;
    }

}
