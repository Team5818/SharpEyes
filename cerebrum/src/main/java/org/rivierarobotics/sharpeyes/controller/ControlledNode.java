package org.rivierarobotics.sharpeyes.controller;

import com.google.auto.value.AutoValue;

import javafx.scene.Node;

@AutoValue
public abstract class ControlledNode<C, N extends Node> {

    public static <C, N extends Node> ControlledNode<C, N> wrap(C controller, N node) {
        return new AutoValue_ControlledNode<>(controller, node);
    }

    ControlledNode() {
    }

    public abstract C getController();

    public abstract N getNode();

}
