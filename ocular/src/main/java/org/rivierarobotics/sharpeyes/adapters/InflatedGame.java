package org.rivierarobotics.sharpeyes.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.protobuf.ByteString;

import org.rivierarobotics.protos.Game;

import javax.annotation.Nullable;

@AutoValue
public abstract class InflatedGame implements Parcelable {

    public static InflatedGame inflate(Game game) {
        return create(game, game.getName(), decodeJpeg(game.getIcon()));
    }

    @Nullable
    private static Bitmap decodeJpeg(@Nullable ByteString icon) {
        if (icon == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(icon.toByteArray(), 0, icon.size());
    }

    public static InflatedGame create(Game base, String name, @Nullable Bitmap icon) {
        return new AutoValue_InflatedGame(base, name, icon);
    }

    InflatedGame() {
    }

    public abstract Game getBase();

    public abstract String getName();

    @Nullable
    public abstract Bitmap getIcon();

}
