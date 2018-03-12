package org.rivierarobotics.sharpeyes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.rivierarobotics.sharpeyes.adapters.InflatedGame;

import static com.google.common.base.Preconditions.checkNotNull;

public class MatchLayoutEditActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView uxArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_layout_edit);

        uxArea = findViewById(R.id.ux_area);

        InflatedGame inflGame = getIntent().getParcelableExtra("game");
        checkNotNull(inflGame, "oh no, i'm missing my game");
        uxArea.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("match-edit", "Surface Created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("match-edit", "Surface Changed");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("match-edit", "Surface Destroyed");
    }
}
