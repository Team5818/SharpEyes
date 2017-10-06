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
package org.rivierarobotics.sharpeyes;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.rivierarobotics.protos.FieldDefinition;
import org.rivierarobotics.protos.FieldValue;
import org.rivierarobotics.protos.Game;
import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.sharpeyes.common.TransmissionDataWriter;
import org.rivierarobotics.sharpeyes.data.transmission.TransmissionDataProvider;

/**
 * Creates data to be used by a {@link TransmissionDataProvider}.
 */
public class TransmissionDataGenerator {

    private static final Path GEN_PATH_BASE = Paths.get("./generated");
    private static final Scanner scanner = new Scanner(System.in);
    private static final Path GAME_REF = FXUtil.fixExtension(GEN_PATH_BASE, SharpEyes.GDEF_EXTENSION);
    private static final Path OUTPUT = FXUtil.fixExtension(GEN_PATH_BASE, SharpEyes.FRTSM_EXTENSION);

    public static void main(String[] args) throws IOException {
        Game game = Game.parseFrom(Files.readAllBytes(GAME_REF));
        int number = getInt("Number of matches");
        List<TeamMatch> matches = IntStream.range(0, number)
                .mapToObj(i -> generateMatch(game, i))
                .collect(toImmutableList());
        new TransmissionDataWriter(matches).writeToPath(OUTPUT);
    }

    private static final Random RNG = new Random();

    private static TeamMatch generateMatch(Game game, int matchNumber) {
        TeamMatch.Builder matchBuilder = TeamMatch.newBuilder()
                .setGame(game.getName())
                .setRegional("Generated Regional")
                .setTeamNumber(getRandomTeam())
                .setMatchNumber(matchNumber);

        game.getFieldDefsList().forEach(field -> {
            matchBuilder.putValues(field.getName(), getValueForType(field));
        });
        return matchBuilder
                .build();
    }

    private static FieldValue getValueForType(FieldDefinition def) {
        FieldValue.Builder fv = FieldValue.newBuilder();
        switch (def.getType()) {
            case STRING:
                fv.setStr("string data: " + RNG.nextDouble());
                break;
            case CHOICE:
                List<String> choices = def.getChoicesList();
                if (choices.isEmpty()) {
                    fv.setStr("");
                    break;
                }
                fv.setStr(choices.get(RNG.nextInt(choices.size())));
                break;
            case BOOLEAN:
                fv.setBoole(RNG.nextBoolean());
                break;
            case FLOATING:
                fv.setFloating(RNG.nextDouble());
                break;
            case INTEGER:
                fv.setInteger(RNG.nextInt(10));
                break;
            default:
                throw new AssertionError("missing case: " + def.getType());
        }
        return fv.build();
    }

    private static int getRandomTeam() {
        return RNG.nextInt(10);
    }

    private static int getInt(String msg) {
        return getInput(msg, Integer::parseInt);
    }

    private static <T> T getInput(String msg, Function<String, T> converter) {
        System.out.print(msg + ": ");
        for (;;) {
            try {
                return converter.apply(scanner.nextLine());
            } catch (RuntimeException e) {
                // a-ok?
            }
        }
    }
}
