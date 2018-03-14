package org.rivierarobotics.sharpeyes.gamedb;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import org.rivierarobotics.protos.Game;

@Entity
public class GameEntity {

    @PrimaryKey
    @NonNull
    private String name;
    @ColumnInfo(name = "data")
    private byte[] data;
    private transient Game game;

    public GameEntity(Game game) {
        this(game.getName());
        this.game = game;
    }

    public GameEntity(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    // ABSOLUTELY HERETICAL -- TODO actually expand Game object?
    public byte[] getData() {
        Log.i("GameEntity", "getting data in getData: " + game);
        return game.toByteArray();
    }

    public void setData(byte[] data) {
        try {
            Log.i("GameEntity", "loading data from setData");
            game = Game.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            Log.e("GameEntity", "Error loading data", e);
        }
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        name = game.getName();
        this.game = game;
    }
}
