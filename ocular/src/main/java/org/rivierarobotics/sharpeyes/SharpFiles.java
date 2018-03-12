package org.rivierarobotics.sharpeyes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

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
