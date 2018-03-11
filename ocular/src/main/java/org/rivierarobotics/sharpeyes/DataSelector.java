package org.rivierarobotics.sharpeyes;

import android.content.Intent;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.protos.Games;
import org.rivierarobotics.protos.Match;
import org.rivierarobotics.protos.Regional;
import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.sharpeyes.adapters.InflatedGame;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Passed down and made more specific with each selection.
 * Used to select game, regional, match info -- at any level.
 */
@AutoValue
public abstract class DataSelector implements Parcelable {

    private static final String EXTRA_KEY = "rrDataSelector";

    public static DataSelector loadFrom(Intent intent) {
        return intent.getParcelableExtra(EXTRA_KEY);
    }

    public void saveTo(Intent intent) {
        intent.putExtra(EXTRA_KEY, this);
    }

    public static Builder builder() {
        return new AutoValue_DataSelector.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder games(Games games);

        public abstract Builder gameId(@Nullable String gameId);

        abstract String gameId();

        public abstract Builder regionalId(@Nullable String regionalId);

        abstract String regionalId();

        public abstract Builder matchNumber(@Nullable Integer matchNumber);

        abstract Integer matchNumber();

        public abstract Builder teamNumber(@Nullable Integer teamNumber);

        abstract Integer teamNumber();

        abstract DataSelector autoBuild();

        public DataSelector build() {
            if (regionalId() != null && gameId() == null) {
                throw new IllegalStateException("Game ID is required for regional ID");
            }
            if (matchNumber() != null && regionalId() == null) {
                throw new IllegalStateException("Regional ID is required for match number");
            }
            if (teamNumber() != null && matchNumber() == null) {
                throw new IllegalStateException("Match number is required for team number");
            }
            return autoBuild();
        }

    }

    DataSelector() {
    }

    abstract Games games();

    @Nullable
    public abstract String gameId();

    @Nullable
    public abstract String regionalId();

    @Nullable
    public abstract Integer matchNumber();

    @Nullable
    public abstract Integer teamNumber();

    @Memoized
    public InflatedGame game() {
        String gameId = gameId();
        checkNotNull(gameId);
        Game game = games().getGamesOrThrow(gameId);
        return InflatedGame.inflate(game);
    }

    @Memoized
    public Regional regional() {
        String regId = regionalId();
        checkNotNull(regId);
        return game().getBase().getRegionalsOrThrow(regId);
    }

    @Memoized
    public Match match() {
        Integer matchNumber = matchNumber();
        checkNotNull(matchNumber);
        return regional().getMatchesOrThrow(matchNumber);
    }

    @Memoized
    public TeamMatch teamMatch() {
        Integer teamNumber = teamNumber();
        checkNotNull(teamNumber);
        return match().getTeamsOrThrow(teamNumber);
    }

    abstract Builder toBuilder();

    public DataSelector selectGame(String game) {
        return toBuilder().gameId(game).build();
    }


    public DataSelector selectRegional(String regional) {
        return toBuilder().regionalId(regional).build();
    }


    public DataSelector selectMatch(int match) {
        return toBuilder().matchNumber(match).build();
    }


    public DataSelector selectTeam(int team) {
        return toBuilder().teamNumber(team).build();
    }


}
