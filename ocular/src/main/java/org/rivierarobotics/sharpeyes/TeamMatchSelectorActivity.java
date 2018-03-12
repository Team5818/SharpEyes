package org.rivierarobotics.sharpeyes;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.sharpeyes.adapters.GenericAdapter;

public class TeamMatchSelectorActivity extends AppCompatActivity {

    private GenericAdapter<TeamMatch> adapter;
    private RecyclerView matchesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_team_select);

        matchesView = findViewById(R.id.listView);
        matchesView.setHasFixedSize(true);
        matchesView.setLayoutManager(new LinearLayoutManager(this));
        matchesView.setAdapter(adapter = new GenericAdapter<>(
                this,
                db -> s -> db.getMatch(s).getTeamsMap().values(),
                t -> getString(R.string.match_x_team_x, getMatchNumber(), t.getTeamNumber()),
                t -> null,
                (teamMatch, selector) -> selector.selectTeam(teamMatch.getTeamNumber()),
                MatchEditActivity.class
        ));
        adapter.initialize(getIntent());

        TextView title = findViewById(R.id.title);
        title.setText(getString(R.string.match_x, getMatchNumber()));
    }

    private int getMatchNumber() {
        return adapter.getDb().getMatch(adapter.getSelector()).getMatchNumber();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GenericAdapter.REQUEST_CODE) {
            adapter.onReloadRequest();
        }
    }

}
