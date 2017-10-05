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
package org.rivierarobotics.sharpeyes.data.qrvid;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.sharpeyes.data.DataProvider;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

@AutoService(DataProvider.class)
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
