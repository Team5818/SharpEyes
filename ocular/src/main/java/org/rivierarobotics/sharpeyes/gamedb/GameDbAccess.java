package org.rivierarobotics.sharpeyes.gamedb;

import android.util.Log;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.protos.Match;
import org.rivierarobotics.protos.Regional;
import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.sharpeyes.DataSelector;
import org.rivierarobotics.sharpeyes.SharpFiles;
import org.rivierarobotics.sharpeyes.adapters.InflatedGame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static com.google.common.base.Preconditions.checkNotNull;

public class GameDbAccess {

    private static GameDbAccess instance;

    public static void initialize(SharpFiles files, GameDbDao dao) {
        instance = new GameDbAccess(files, dao);
    }

    public static GameDbAccess getInstance() {
        checkNotNull(instance, "Un-initialized!");
        return instance;
    }

    private final ExecutorService dbAccess = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                    .setNameFormat("db-access-%d")
                    .build()
    );
    private final Map<String, InflatedGame> games = Collections.synchronizedMap(new LinkedHashMap<>());
    private final SharpFiles files;
    private final GameDbDao dao;

    private GameDbAccess(SharpFiles files, GameDbDao dao) {
        this.files = files;
        this.dao = dao;
    }

    public Map<String, InflatedGame> getGames() {
        return games;
    }

    public InflatedGame getGame(DataSelector selector) {
        return games.computeIfAbsent(selector.gameId(), gid -> InflatedGame.inflate(Game.newBuilder().setName(gid).build()));
    }

    public InflatedGame rebuildGame(DataSelector selector, UnaryOperator<Game.Builder> config) {
        Game.Builder b = getGame(selector).getBase().toBuilder();
        b = config.apply(b);
        InflatedGame g = InflatedGame.inflate(b.build());
        saveGame(g);
        games.put(b.getName(), g);
        return g;
    }

    private void saveGame(InflatedGame g) {
        Log.d("GameDbAccess", "Saving game: " + g.getName());
        dbAccess.submit(() -> dao.insert(g.getBase()));
        File gameFile = files.getSavedGameFile(g.getName());
        try (OutputStream stream = new FileOutputStream(gameFile)) {
            g.getBase().writeTo(stream);
        } catch (IOException e) {
            Log.e("GameDbAccess", "Error saving game", e);
            return;
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

    public Regional rebuildRegional(DataSelector selector, UnaryOperator<Regional.Builder> config) {
        Regional.Builder b = getRegional(selector).toBuilder();
        b = config.apply(b);
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

    public Match rebuildMatch(DataSelector selector, UnaryOperator<Match.Builder> config) {
        Match.Builder b = getMatch(selector).toBuilder();
        b = config.apply(b);
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

    public TeamMatch rebuildTeamMatch(DataSelector selector, UnaryOperator<TeamMatch.Builder> config) {
        TeamMatch.Builder b = getTeamMatch(selector).toBuilder();
        b = config.apply(b);
        TeamMatch tm = b.build();
        rebuildMatch(selector, t -> t.putTeams(tm.getTeamNumber(), tm));
        return tm;
    }
}
