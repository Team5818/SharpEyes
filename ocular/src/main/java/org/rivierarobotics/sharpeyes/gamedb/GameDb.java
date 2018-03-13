package org.rivierarobotics.sharpeyes.gamedb;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import static com.google.common.base.Preconditions.checkNotNull;

@Database(entities = {GameEntity.class}, version = 1, exportSchema = false)
public abstract class GameDb extends RoomDatabase {

    private static GameDb INSTANCE;

    public static void initialize(Context context) {
        INSTANCE = Room.databaseBuilder(context, GameDb.class, "sharpeyes-gamedb.db")
                .build();
    }

    public static GameDb getInstance() {
        checkNotNull(INSTANCE, "Un-initialized!");
        return INSTANCE;
    }

    public abstract GameDbDao getDao();

}
