package org.rivierarobotics.sharpeyes;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.protos.Regional;
import org.rivierarobotics.sharpeyes.adapters.SelectorAdapter;
import org.rivierarobotics.sharpeyes.adapters.InflatedGame;
import org.rivierarobotics.sharpeyes.gamedb.GameDb;
import org.rivierarobotics.sharpeyes.gamedb.GameDbAccess;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RegionalSelectorActivity extends AppCompatActivity implements NameDialogFragment.NDFCallback {

    private SelectorAdapter<Regional> adapter;
    private RecyclerView regionalsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            newObjSelector = savedInstanceState.getParcelable("selector");
        }
        DataBindingUtil.setContentView(this, R.layout.activity_regional_select);

        regionalsView = findViewById(R.id.listView);
        regionalsView.setHasFixedSize(true);
        regionalsView.setLayoutManager(new LinearLayoutManager(this));
        regionalsView.setAdapter(adapter = new SelectorAdapter<>(
                this,
                Comparator.comparing(Regional::getName),
                db -> s -> db.getGame(s).getBase().getRegionalsMap().values(),
                Regional::getName,
                x -> null,
                (regional, selector) -> selector.selectRegional(regional.getName()),
                this::displayNewDialog,
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
            AndroidUtil.startActivityForResult(this, SelectorAdapter.REQUEST_CODE, GameSendActivity.class,
                    intent -> {
                        adapter.getSelector().saveTo(intent);
                    });
        });
    }

    private DataSelector newObjSelector;

    private void displayNewDialog(DataSelector selector) {
        NameDialogFragment frag = new NameDialogFragment();
        frag.setArguments(NameDialogFragment.create(
                getString(R.string.newRegional),
                getString(R.string.enterName),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE));
        newObjSelector = selector;
        frag.show(getFragmentManager(), "new-regional");
    }

    @Override
    public void onText(String text) {
        if (newObjSelector == null) {
            return;
        }
        GameDbAccess.getInstance().rebuildRegional(newObjSelector.selectRegional(text), b -> b);
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
