package org.rivierarobotics.sharpeyes.event;

import com.google.auto.value.AutoValue;

import javafx.scene.Node;

@AutoValue
public abstract class DeleteFieldEvent {

    public static DeleteFieldEvent create(Node deletedField) {
        return new AutoValue_DeleteFieldEvent(deletedField);
    }

    DeleteFieldEvent() {
    }

    public abstract Node deletedField();

}
