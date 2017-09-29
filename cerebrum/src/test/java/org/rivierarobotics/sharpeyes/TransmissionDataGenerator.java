package org.rivierarobotics.sharpeyes;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;

import org.rivierarobotics.protos.FieldDefinition;
import org.rivierarobotics.protos.FieldValue;
import org.rivierarobotics.protos.Game;
import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.protos.TransmitFrame;
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
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(OUTPUT))) {
            TransmitFrame.newBuilder()
                    .setStart(true)
                    .build().writeDelimitedTo(out);
            for (int i = 0; i < number; i++) {
                TransmitFrame.newBuilder()
                        .setMatch(generateMatch(game, i))
                        .build().writeDelimitedTo(out);
            }
            TransmitFrame.newBuilder()
                    .setEnd(true)
                    .build().writeDelimitedTo(out);
        }
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
