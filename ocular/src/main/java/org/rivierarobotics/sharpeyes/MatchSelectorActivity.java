package org.rivierarobotics.sharpeyes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import org.rivierarobotics.protos.Match;
import org.rivierarobotics.sharpeyes.adapters.GenericAdapter;

public class MatchSelectorActivity extends AppCompatActivity {

    private GenericAdapter<Match> adapter;
    private RecyclerView matchesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headered_list);

        matchesView = findViewById(R.id.listView);
        matchesView.setHasFixedSize(true);
        matchesView.setLayoutManager(new LinearLayoutManager(this));
        matchesView.setAdapter(adapter = new GenericAdapter<>(
                this,
                selector -> selector.regional().getMatchesMap().values(),
                m -> String.valueOf(m.getMatchNumber()),
                m -> null,
                (match, selector) -> selector.selectMatch(match.getMatchNumber()),
                MatchEditActivity.class
        ));
        DataSelector selector = DataSelector.loadFrom(getIntent());

        TextView title = findViewById(R.id.title);
        title.setText(selector.regional().getName());
        adapter.initialize(selector);
    }

}
