package org.rivierarobotics.sharpeyes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.protos.Match;
import org.rivierarobotics.protos.Regional;
import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.sharpeyes.adapters.InflatedGame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public class GameDb implements Parcelable {

    private static final String EXTRA_KEY = "rrGameDb";

    public static GameDb loadFrom(Intent intent) {
        return checkNotNull(intent.getParcelableExtra(EXTRA_KEY));
    }

    public void saveTo(Intent intent) {
        intent.putExtra(EXTRA_KEY, this);
    }

    public static final Parcelable.Creator<GameDb> CREATOR = new Parcelable.Creator<GameDb>() {
        @Override
        public GameDb createFromParcel(Parcel in) {
            File root = (File) in.readSerializable();
            GameDb db = new GameDb(SharpFiles.setup(root));
            Bundle bundle = in.readBundle(getClass().getClassLoader());
            for (String s : bundle.keySet()) {
                db.games.put(s, bundle.getParcelable(s));
            }
            return db;
        }

        @Override
        public GameDb[] newArray(int size) {
            return new GameDb[size];
        }
    };

    private final Map<String, InflatedGame> games = new LinkedHashMap<>();
    private final SharpFiles files;

    public GameDb(SharpFiles files) {
        this.files = files;
    }

    public Map<String, InflatedGame> getGames() {
        return games;
    }

    public InflatedGame getGame(DataSelector selector) {
        return games.computeIfAbsent(selector.gameId(), gid -> InflatedGame.inflate(Game.newBuilder().setName(gid).build()));
    }

    public InflatedGame rebuildGame(DataSelector selector, Consumer<Game.Builder> config) {
        Game.Builder b = getGame(selector).getBase().toBuilder();
        config.accept(b);
        InflatedGame g = InflatedGame.inflate(b.build());
        saveGame(g);
        games.put(b.getName(), g);
        return g;
    }

    private void saveGame(InflatedGame g) {
        File gameFile = files.getSavedGameFile(g.getName());
        try (OutputStream stream = new FileOutputStream(gameFile)) {
            g.getBase().writeTo(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Regional getRegional(DataSelector selector) {
        Game game = getGame(selector).getBase();
        Regional regional = game.getRegionalsOrDefault(selector.regionalId(), null);
        if (regional == null) {
            game = rebuildGame(selector, g -> g.putRegionals(selector.regionalId(), Regional.newBuilder().setName(selector.regionalId()).build())).getBase();
            regional = game.getRegionalsOrThrow(selector.regionalId());
        }
        return regional;
    }

    public Regional rebuildRegional(DataSelector selector, Consumer<Regional.Builder> config) {
        Regional.Builder b = getRegional(selector).toBuilder();
        config.accept(b);
        Regional r = b.build();
        rebuildGame(selector, g -> g.putRegionals(r.getName(), r));
        return r;
    }

    public Match getMatch(DataSelector selector) {
        Regional regional = getRegional(selector);
        Integer matchNum = checkNotNull(selector.matchNumber());
        Match match = regional.getMatchesOrDefault(matchNum, null);
        if (match == null) {
            regional = rebuildRegional(selector, r -> r.putMatches(matchNum, Match.newBuilder().setMatchNumber(matchNum).build()));
            match = regional.getMatchesOrThrow(matchNum);
        }
        return match;
    }

    public Match rebuildMatch(DataSelector selector, Consumer<Match.Builder> config) {
        Match.Builder b = getMatch(selector).toBuilder();
        config.accept(b);
        Match m = b.build();
        rebuildRegional(selector, r -> r.putMatches(m.getMatchNumber(), m));
        return m;
    }

    public TeamMatch getTeamMatch(DataSelector selector) {
        Match match = getMatch(selector);
        Integer teamNum = checkNotNull(selector.teamNumber());
        TeamMatch teamMatch = match.getTeamsOrDefault(teamNum, null);
        if (teamMatch == null) {
            match = rebuildMatch(selector, m -> m.putTeams(teamNum, TeamMatch.newBuilder().setTeamNumber(teamNum).build()));
            teamMatch = match.getTeamsOrThrow(teamNum);
        }
        return teamMatch;
    }

    public TeamMatch rebuildTeamMatch(DataSelector selector, Consumer<TeamMatch.Builder> config) {
        TeamMatch.Builder b = getTeamMatch(selector).toBuilder();
        config.accept(b);
        TeamMatch tm = b.build();
        rebuildMatch(selector, t -> t.putTeams(tm.getTeamNumber(), tm));
        return tm;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(files.getRoot());
        Bundle bundle = new Bundle();
        games.forEach(bundle::putParcelable);
        dest.writeBundle(bundle);
    }
}
