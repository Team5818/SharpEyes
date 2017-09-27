package org.rivierarobotics.sharpeyes.event;

import com.google.auto.value.AutoValue;

import javafx.scene.Node;

@AutoValue
public abstract class AddTabEvent {

    public static AddTabEvent create(String name, Node contents) {
        return new AutoValue_AddTabEvent(name, contents);
    }

    AddTabEvent() {
    }

    public abstract String getTabName();

    public abstract Node getTabContents();

}
