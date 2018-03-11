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


import org.rivierarobotics.protos.CompactTeamMatch;
import org.rivierarobotics.protos.FieldValue;
import org.rivierarobotics.protos.Game;
import org.rivierarobotics.protos.Match;
import org.rivierarobotics.protos.Regional;
import org.rivierarobotics.protos.TeamMatch;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class GameTreeMerger {

    public static GameTreeMerger startingWith(Game instance) {
        return new GameTreeMerger(instance);
    }

    private Game currentInstance;

    private GameTreeMerger(Game currentInstance) {
        this.currentInstance = currentInstance;
    }

    public Game getCurrentInstance() {
        return currentInstance;
    }

    public GameTreeMerger merge(CompactTeamMatch ctm) {
        if (!ctm.getGameName().equals(currentInstance.getName())) {
            throw new IllegalArgumentException("Incorrect game for TeamMatch");
        }
        Game.Builder builder = currentInstance.toBuilder();
        Regional.Builder regional = Optional.of(builder.getRegionalsMap().get(ctm.getRegionalName()))
                .orElseThrow(() -> new IllegalArgumentException("Incorrect regional for TeamMatch"))
                .toBuilder();


        Match.Builder match = Optional.of(regional.getMatchesMap().get(ctm.getMatchNumber()))
                .orElseThrow(() -> new IllegalArgumentException("Incorrect match number for TeamMatch"))
                .toBuilder();

        match.putTeams(ctm.getTeamNumber(), TeamMatch.newBuilder().setTeamNumber(ctm.getTeamNumber()).putAllValues(ctm.getValuesMap()).build());

        regional.putMatches(ctm.getMatchNumber(), match.build());
        builder.putRegionals(ctm.getRegionalName(), regional.build());
        currentInstance = builder.build();
        return this;
    }

    public Stream<CompactTeamMatch> getAllMatches() {
        return currentInstance.getRegionalsMap().values().stream()
                .flatMap(reg -> reg.getMatchesMap().values().stream()
                        .flatMap(m -> m.getTeamsMap().values().stream()
                                .map(tm -> newCTM(currentInstance.getName(), reg.getName(), m.getMatchNumber(), tm.getTeamNumber(), tm.getValuesMap()))));
    }

    private static CompactTeamMatch newCTM(String gameName, String regName, int matchNumber, int teamNumber, Map<String, FieldValue> valuesMap) {
        return CompactTeamMatch.newBuilder()
                .setGameName(gameName)
                .setRegionalName(regName)
                .setMatchNumber(matchNumber)
                .setTeamNumber(teamNumber)
                .putAllValues(valuesMap)
                .build();
    }
}
