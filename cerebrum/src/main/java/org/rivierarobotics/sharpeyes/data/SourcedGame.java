package org.rivierarobotics.sharpeyes.data;

import java.nio.file.Path;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.sharpeyes.Loader;

import com.google.auto.value.AutoValue;

/**
 * A sourced game includes the original {@link Path} of the game.
 */
@AutoValue
public abstract class SourcedGame {
    
    public static SourcedGame load(Path source) {
        return wrap(source, Loader.getGame(source));
    }
    
    public static SourcedGame wrap(Path source, Game game) {
        return new AutoValue_SourcedGame(source, game);
    }

    SourcedGame() {
    }

    public abstract Path getSource();

    public abstract Game getGame();

}
