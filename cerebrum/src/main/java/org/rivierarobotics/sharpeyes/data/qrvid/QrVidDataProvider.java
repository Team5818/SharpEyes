package org.rivierarobotics.sharpeyes.data.qrvid;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.sharpeyes.data.DataProvider;

import com.google.common.collect.ImmutableList;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class QrVidDataProvider implements DataProvider {

    private static final String START = "Please put the QR into the camera view.";

    private final ObjectProperty<String> currentMessage = new SimpleObjectProperty<>(this, "currentMessage");

    private final Label messageLabel = new Label();
    {
        messageLabel.textProperty().bind(currentMessage);
    }

    private final ExecutorService exec = Executors.newCachedThreadPool();

    @Override
    public CompletableFuture<List<TeamMatch>> provideMatches() {
        Stage stage = new Stage();
        currentMessage.set(START);
        stage.setScene(new Scene(messageLabel, 800, 600));
        stage.show();
        return CompletableFuture.supplyAsync(this::doProvideMatches, exec)
                .whenComplete((w, a) -> stage.close());
    }
    
    private void asyncSetMessage(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    private List<TeamMatch> doProvideMatches() {
        ImmutableList.Builder<TeamMatch> matches = ImmutableList.builder();
        asyncSetMessage(START);
        negotiateStart();
        return matches.build();
    }

    private void negotiateStart() {
    }

}
