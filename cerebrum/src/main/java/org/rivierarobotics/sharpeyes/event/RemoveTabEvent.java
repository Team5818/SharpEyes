package org.rivierarobotics.sharpeyes.event;

import com.google.auto.value.AutoValue;

import javafx.scene.Node;

@AutoValue
public abstract class RemoveTabEvent {

    public static RemoveTabEvent create(Node contents) {
        return new AutoValue_RemoveTabEvent(contents);
    }

    RemoveTabEvent() {
    }

    public abstract Node getTabContents();

}
