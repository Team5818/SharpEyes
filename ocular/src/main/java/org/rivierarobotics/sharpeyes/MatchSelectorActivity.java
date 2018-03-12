package org.rivierarobotics.sharpeyes;

import android.content.Intent;
import android.databinding.DataBindingUtil;
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
        DataBindingUtil.setContentView(this, R.layout.activity_match_select);

        matchesView = findViewById(R.id.listView);
        matchesView.setHasFixedSize(true);
        matchesView.setLayoutManager(new LinearLayoutManager(this));
        matchesView.setAdapter(adapter = new GenericAdapter<>(
                this,
                db -> s -> db.getRegional(s).getMatchesMap().values(),
                m -> getString(R.string.match_x, m.getMatchNumber()),
                m -> null,
                (match, selector) -> selector.selectMatch(match.getMatchNumber()),
                TeamMatchSelectorActivity.class
        ));
        adapter.initialize(getIntent());

        TextView title = findViewById(R.id.title);
        title.setText(getString(R.string.x_regional, adapter.getDb().getRegional(adapter.getSelector()).getName()));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GenericAdapter.REQUEST_CODE) {
            adapter.onReloadRequest();
        }
    }
}
