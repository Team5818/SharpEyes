package org.rivierarobotics.sharpeyes.data.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import org.rivierarobotics.protos.TransmitFrame;
import org.rivierarobotics.sharpeyes.data.transmission.TransmissionDataProvider;

import com.google.common.collect.AbstractIterator;

/**
 * Simpler version of {@link InteractiveFileDataProvider} which loads from a
 * programatically specified file instead.
 */
public class FileDataProvider extends TransmissionDataProvider {

    private final Path src;

    public FileDataProvider(Path src) {
        this.src = src;
    }

    @Override
    protected CompletableFuture<Iterator<TransmitFrame>> getFrames() {
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
