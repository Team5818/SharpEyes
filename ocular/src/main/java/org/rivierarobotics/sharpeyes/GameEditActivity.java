package org.rivierarobotics.sharpeyes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.sharpeyes.adapters.GameEditAdapter;
import org.rivierarobotics.sharpeyes.adapters.InflatedGame;

import static com.google.common.base.Preconditions.checkNotNull;

public class GameEditActivity extends AppCompatActivity {

    private GameEditAdapter adapter;
    private RecyclerView prefsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        prefsView = findViewById(R.id.listView);
        prefsView.setHasFixedSize(true);
        prefsView.setLayoutManager(new LinearLayoutManager(this));
        prefsView.setAdapter(adapter = new GameEditAdapter());

        InflatedGame inflGame = getIntent().getParcelableExtra("game");
        checkNotNull(inflGame, "oh no, i'm missing my game");
        Game game = inflGame.getBase();

        ImageView image = findViewById(R.id.icon);
        if (inflGame.getIcon() != null) {
            image.setImageBitmap(inflGame.getIcon());
        }
        TextView title = findViewById(R.id.title);
        title.setText(game.getName());
        adapter.addItem(new GameEditAdapter.Item("Edit Match Layout", view -> {
            AndroidUtil.startActivity(view, MatchLayoutEditActivity.class,
                    intent -> intent.putExtra("game", inflGame));
        }));
    }

}
