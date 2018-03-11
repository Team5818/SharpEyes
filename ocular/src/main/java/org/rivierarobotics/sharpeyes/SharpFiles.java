package org.rivierarobotics.sharpeyes;

import android.content.Context;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class SharpFiles {

    private static final String GAME_DEF_NAME = "this.gmdef";

    private static File createDirOrDie(File root, String sub) {
        File next = new File(root, sub);
        if (!next.exists() && !next.mkdir()) {
            throw new IllegalStateException("Unable to create directory in " + root + " named " + sub);
        }
        return next;
    }

    public static SharpFiles setup(Context context) {
        File appRoot = context.getFilesDir();
        File sfRoot = createDirOrDie(appRoot, "games");
        return new SharpFiles(sfRoot);
    }

    private final File root;

    private SharpFiles(File root) {
        this.root = root;
    }

    public List<File> getSavedGameFiles() {
        return ImmutableList.copyOf(root.listFiles()).stream()
                .map(gameDir -> new File(gameDir, GAME_DEF_NAME))
                .filter(File::exists)
                .collect(toImmutableList());
    }

    public File getSavedGameFile(String name) {
        File gameRoot = createDirOrDie(root, name);
        return new File(gameRoot, GAME_DEF_NAME);
    }

    public File getMatchFile(String gameName, String regionalName, int matchNumber, int teamNumber) {
        File gameRoot = createDirOrDie(root, gameName);
        File regionalRoot = createDirOrDie(gameRoot, regionalName);
        File matchRoot = createDirOrDie(regionalRoot, String.valueOf(matchNumber));
        return new File(matchRoot, teamNumber + ".mtch");
    }
}
