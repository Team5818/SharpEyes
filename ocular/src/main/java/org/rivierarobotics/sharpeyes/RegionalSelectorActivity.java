package org.rivierarobotics.sharpeyes;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.rivierarobotics.protos.Regional;
import org.rivierarobotics.sharpeyes.adapters.GenericAdapter;
import org.rivierarobotics.sharpeyes.adapters.InflatedGame;

public class RegionalSelectorActivity extends AppCompatActivity {

    private GenericAdapter<Regional> adapter;
    private RecyclerView regionalsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_regional_select);

        regionalsView = findViewById(R.id.listView);
        regionalsView.setHasFixedSize(true);
        regionalsView.setLayoutManager(new LinearLayoutManager(this));
        regionalsView.setAdapter(adapter = new GenericAdapter<>(
                this,
                db -> s -> db.getGame(s).getBase().getRegionalsMap().values(),
                Regional::getName,
                x -> null,
                (regional, selector) -> selector.selectRegional(regional.getName()),
                MatchSelectorActivity.class
        ));

        adapter.initialize(getIntent());

        InflatedGame inflGame = adapter.getDb().getGame(adapter.getSelector());

        ImageView image = findViewById(R.id.icon);
        if (inflGame.getIcon() != null) {
            image.setImageBitmap(inflGame.getIcon());
        }
        TextView title = findViewById(R.id.title);
        title.setText(inflGame.getName());

        Button editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(view -> {
            AndroidUtil.startActivity(view, GameEditActivity.class,
                    intent -> intent.putExtra("game", inflGame));
        });

        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(view -> {
            AndroidUtil.startActivityForResult(this, GenericAdapter.REQUEST_CODE, GameSendActivity.class,
                    intent -> {
                        adapter.getDb().saveTo(intent);
                        adapter.getSelector().saveTo(intent);
                    });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GenericAdapter.REQUEST_CODE) {
            adapter.onReloadRequest();
        }
    }

}
