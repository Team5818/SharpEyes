package org.rivierarobotics.sharpeyes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.rivierarobotics.sharpeyes.adapters.MatchFieldAdapter;

/**
 * The primary activity.
 */
public class MatchEditActivity extends AppCompatActivity {

    private MatchFieldAdapter adapter;
    private RecyclerView matchesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_edit);

        adapter = new MatchFieldAdapter();

        matchesView = findViewById(R.id.listView);
        matchesView.setHasFixedSize(true);
        matchesView.setLayoutManager(new LinearLayoutManager(this));
        matchesView.setAdapter(adapter);

        adapter.initialize(getIntent());

        TextView title = findViewById(R.id.title);
        title.setText(getString(R.string.match_x_team_x,
                adapter.getDb().getMatch(adapter.getSelector()).getMatchNumber(),
                adapter.getDb().getTeamMatch(adapter.getSelector()).getTeamNumber()));

        Button save = findViewById(R.id.saveButton);
        save.setOnClickListener(view -> {
            saveData();
            showSaveToast();
        });
    }

    private void saveData() {
        adapter.saveData(matchesView);
    }

    @Override
    protected void onPause() {
        saveData();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // only display toast on exit, otherwise it might be a little spammy
        showSaveToast();
    }

    private void showSaveToast() {
        Toast.makeText(this, R.string.notifications_save, Toast.LENGTH_SHORT).show();
    }
}
