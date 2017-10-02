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
package org.rivierarobotics.sharpeyes.data.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import org.rivierarobotics.protos.TransmitFrame;
import org.rivierarobotics.sharpeyes.FXUtil;
import org.rivierarobotics.sharpeyes.SharpEyes;
import org.rivierarobotics.sharpeyes.data.transmission.TransmissionDataProvider;

import com.google.common.collect.AbstractIterator;

import javafx.stage.FileChooser;
import javafx.stage.Window;

public class FileDataProvider extends TransmissionDataProvider {

    private final FileChooser srcChooser = new FileChooser();
    {
        srcChooser.getExtensionFilters().clear();
        srcChooser.getExtensionFilters().add(SharpEyes.FRAME_TRANSMISSIONS);
        srcChooser.setTitle("Choose a Transmission");
    }

    private final Window parentWindow;

    public FileDataProvider(Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    private Path requestSource() {
        File selected = srcChooser.showOpenDialog(parentWindow);
        if (selected == null) {
            return null;
        }
        return FXUtil.fixExtension(selected.toPath(), SharpEyes.FRTSM_EXTENSION);
    }

    @Override
    protected CompletableFuture<Iterator<TransmitFrame>> getFrames() {
        Path src = requestSource();
        if (src == null) {
            CompletableFuture<Iterator<TransmitFrame>> f = new CompletableFuture<>();
            f.cancel(true);
            return f;
        }
        return CompletableFuture.supplyAsync(() -> {
            return new AbstractIterator<TransmitFrame>() {

                private InputStream stream;

                @Override
                protected TransmitFrame computeNext() {
                    try {
                        if (stream == null) {
                            stream = new BufferedInputStream(Files.newInputStream(src));
                        }
                        return TransmitFrame.parseDelimitedFrom(stream);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }

            };
        });
    }

}
