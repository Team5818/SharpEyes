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
package org.rivierarobotics.sharpeyes.data.transmission;

import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.protos.TransmitFrame;
import org.rivierarobotics.protos.TransmitFrame.KindCase;
import org.rivierarobotics.sharpeyes.data.DataProvider;
import org.rivierarobotics.sharpeyes.data.ImportedMatches;

import com.google.common.collect.ImmutableList;

public abstract class TransmissionDataProvider implements DataProvider {

    protected static final class ImportedFrames {

        private final String name;
        private final Iterator<TransmitFrame> frames;

        public ImportedFrames(String name, Iterator<TransmitFrame> frames) {
            super();
            this.name = name;
            this.frames = frames;
        }

    }

    @Override
    public CompletableFuture<ImportedMatches> provideMatches() {
        return getFrames().thenApply(imported -> {
            Iterator<TransmitFrame> frames = imported.frames;
            ImmutableList.Builder<TeamMatch> b = ImmutableList.builder();
            checkState(frames.hasNext(), "no frames!");
            TransmitFrame tf = frames.next();
            checkState(tf.getKindCase() == KindCase.START, "incorrect start message!");
            while (frames.hasNext()) {
                tf = frames.next();
                checkState(tf.getKindCase() != KindCase.START, "duplicate start message!");
                if (tf.getKindCase() == KindCase.END) {
                    break;
                }
                b.add(tf.getMatch());
            }
            checkState(tf.getKindCase() == KindCase.END, "improperly ended stream!");
            return ImportedMatches.wrap(imported.name, b.build());
        });
    }

    protected abstract CompletableFuture<ImportedFrames> getFrames();

}
