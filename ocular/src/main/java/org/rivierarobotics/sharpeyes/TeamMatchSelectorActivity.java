package org.rivierarobotics.sharpeyes;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.widget.TextView;

import org.rivierarobotics.protos.Match;
import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.sharpeyes.adapters.SelectorAdapter;
import org.rivierarobotics.sharpeyes.gamedb.GameDbAccess;

import java.util.Comparator;

public class TeamMatchSelectorActivity extends AppCompatActivity implements NameDialogFragment.NDFCallback {

    private SelectorAdapter<TeamMatch> adapter;
    private RecyclerView matchesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            newObjSelector = savedInstanceState.getParcelable("selector");
        }
        DataBindingUtil.setContentView(this, R.layout.activity_team_select);

        matchesView = findViewById(R.id.listView);
        matchesView.setHasFixedSize(true);
        matchesView.setLayoutManager(new LinearLayoutManager(this));
        matchesView.setAdapter(adapter = new SelectorAdapter<>(
                this,
                Comparator.comparingInt(TeamMatch::getTeamNumber),
                db -> s -> db.getMatch(s).getTeamsMap().values(),
                t -> getString(R.string.match_x_team_x, getMatchNumber(), t.getTeamNumber()),
                t -> null,
                (teamMatch, selector) -> selector.selectTeam(teamMatch.getTeamNumber()),
                this::displayNewDialog,
                MatchEditActivity.class
        ));
        adapter.initialize(getIntent());

        TextView title = findViewById(R.id.title);
        title.setText(getString(R.string.match_x, getMatchNumber()));
    }

    private int getMatchNumber() {
        return adapter.getDb().getMatch(adapter.getSelector()).getMatchNumber();
    }

    private DataSelector newObjSelector;

    private void displayNewDialog(DataSelector selector) {
        NameDialogFragment frag = new NameDialogFragment();
        frag.setArguments(NameDialogFragment.create(
                getString(R.string.newTeam),
                getString(R.string.enterNumber),
                InputType.TYPE_CLASS_NUMBER));
        newObjSelector = selector;
        frag.show(getFragmentManager(), "new-team");
    }

    @Override
    public void onText(String text) {
        if (newObjSelector == null) {
            return;
        }
        GameDbAccess.getInstance().rebuildTeamMatch(newObjSelector.selectTeam(Integer.parseInt(text)), b -> b);
        adapter.reloadData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("selector", newObjSelector);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SelectorAdapter.REQUEST_CODE) {
            adapter.onReloadRequest();
        }
    }

}
