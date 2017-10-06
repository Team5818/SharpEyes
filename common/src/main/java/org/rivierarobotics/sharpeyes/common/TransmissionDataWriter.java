/*
 * This file is part of common, licensed under the MIT License (MIT).
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
package org.rivierarobotics.sharpeyes.common;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.protos.TransmitFrame;

import com.google.common.collect.ImmutableList;

public final class TransmissionDataWriter {

    private final List<TeamMatch> matches;

    public TransmissionDataWriter(List<TeamMatch> matches) {
        this.matches = ImmutableList.copyOf(matches);
    }

    public void writeToPath(Path path) throws IOException {
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
            TransmitFrame.newBuilder()
                    .setStart(true)
                    .build().writeDelimitedTo(out);
            for (int i = 0; i < matches.size(); i++) {
                TransmitFrame.newBuilder()
                        .setMatch(matches.get(i))
                        .build().writeDelimitedTo(out);
            }
            TransmitFrame.newBuilder()
                    .setEnd(true)
                    .build().writeDelimitedTo(out);
        }
    }

}
