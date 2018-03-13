package org.rivierarobotics.sharpeyes.gamedb;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.rivierarobotics.protos.Game;

import java.util.List;

@Dao
public interface GameDbDao {

    @Query("SELECT * FROM GameEntity")
    List<GameEntity> getAll();

    @Query("SELECT * FROM GameEntity WHERE name = (:name) LIMIT 1")
    GameEntity getByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GameEntity game);

    default void insert(Game game) {
        GameEntity ge = new GameEntity(game);
        ge.setGame(game);
        insert(ge);
    }

}
