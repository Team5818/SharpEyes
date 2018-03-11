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
package org.rivierarobotics.sharpeyes.data;

import java.nio.file.Path;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.sharpeyes.Loader;
import org.rivierarobotics.sharpeyes.common.GameTreeMerger;

import com.google.auto.value.AutoValue;

/**
 * A sourced game includes the original {@link Path} of the game.
 */
@AutoValue
public abstract class SourcedGame {
    
    public static SourcedGame load(Path source) {
        return wrap(source, GameTreeMerger.startingWith(Loader.getGame(source)));
    }
    
    public static SourcedGame wrap(Path source, GameTreeMerger game) {
        return new AutoValue_SourcedGame(source, game);
    }

    SourcedGame() {
    }

    public abstract Path getSource();

    public abstract GameTreeMerger getGame();

}
