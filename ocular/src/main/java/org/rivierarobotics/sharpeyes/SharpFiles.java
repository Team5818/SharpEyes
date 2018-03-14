package org.rivierarobotics.sharpeyes;

import android.app.Activity;
import android.os.Environment;

import com.google.common.collect.FluentIterable;

import java.io.File;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public class SharpFiles {

    private static final String GAME_DEF_NAME = "this.gmdef";

    private static File createDirOrDie(File root, String sub) {
        File next = new File(root, sub);
        mkDir(next);
        return next;
    }

    private static void mkDir(File next) {
        if (!next.exists() && !next.mkdir()) {
            throw new IllegalStateException("Unable to create directory " + next);
        }
    }

    public static SharpFiles setup(Activity activity) {
        checkState(isExternalStorageWritable(), "no external storage?");
        File docRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        mkDir(docRoot);
        File appRoot = createDirOrDie(docRoot, activity.getPackageName());
        File sfRoot = createDirOrDie(appRoot, "games");
        return setup(sfRoot);
    }


    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static SharpFiles setup(File root) {
        return new SharpFiles(root);
    }

    private final File root;

    private SharpFiles(File root) {
        this.root = root;
    }

    public File getRoot() {
        return root;
    }

    public List<File> getSavedGameFiles() {
        return FluentIterable.from(root.listFiles())
                .transform(gameDir -> new File(gameDir, GAME_DEF_NAME))
                .filter(File::exists)
                .toList();
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
