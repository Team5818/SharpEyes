package org.rivierarobotics.sharpeyes.fx;

import java.nio.file.Path;

import javafx.scene.control.ListCell;

public class PathListCell extends ListCell<Path> {
    @Override
    protected void updateItem(Path item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.getFileName().toString());
        }
    }
}
