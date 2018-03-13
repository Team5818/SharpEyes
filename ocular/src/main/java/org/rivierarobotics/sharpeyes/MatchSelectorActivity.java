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
import org.rivierarobotics.sharpeyes.adapters.SelectorAdapter;
import org.rivierarobotics.sharpeyes.gamedb.GameDbAccess;

import java.util.Comparator;

public class MatchSelectorActivity extends AppCompatActivity implements NameDialogFragment.NDFCallback {

    private SelectorAdapter<Match> adapter;
    private RecyclerView matchesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            newObjSelector = savedInstanceState.getParcelable("selector");
        }
        DataBindingUtil.setContentView(this, R.layout.activity_match_select);

        matchesView = findViewById(R.id.listView);
        matchesView.setHasFixedSize(true);
        matchesView.setLayoutManager(new LinearLayoutManager(this));
        matchesView.setAdapter(adapter = new SelectorAdapter<>(
                this,
                Comparator.comparingInt(Match::getMatchNumber),
                db -> s -> db.getRegional(s).getMatchesMap().values(),
                m -> getString(R.string.match_x, m.getMatchNumber()),
                m -> null,
                (match, selector) -> selector.selectMatch(match.getMatchNumber()),
                this::displayNewDialog,
                TeamMatchSelectorActivity.class
        ));
        adapter.initialize(getIntent());

        TextView title = findViewById(R.id.title);
        title.setText(getString(R.string.x_regional, adapter.getDb().getRegional(adapter.getSelector()).getName()));
    }

    private DataSelector newObjSelector;

    private void displayNewDialog(DataSelector selector) {
        NameDialogFragment frag = new NameDialogFragment();
        frag.setArguments(NameDialogFragment.create(
                getString(R.string.newMatch),
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
        GameDbAccess.getInstance().rebuildMatch(newObjSelector.selectMatch(Integer.parseInt(text)), b -> b);
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
