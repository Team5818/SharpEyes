package org.rivierarobotics.sharpeyes;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.rivierarobotics.protos.FieldDefinition;
import org.rivierarobotics.protos.FieldValue;
import org.rivierarobotics.protos.Game;
import org.rivierarobotics.protos.Match;
import org.rivierarobotics.protos.Regional;
import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.sharpeyes.adapters.GenericAdapter;
import org.rivierarobotics.sharpeyes.adapters.InflatedGame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class GameSelectorActivity extends AppCompatActivity {

    private SharpFiles sharpFiles;
    private GenericAdapter<InflatedGame> adapter;
    private RecyclerView gamesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_select);

        gamesView = findViewById(R.id.listView);
        gamesView.setHasFixedSize(true);
        gamesView.setLayoutManager(new LinearLayoutManager(this));
        gamesView.setAdapter(adapter = new GenericAdapter<>(
                this,
                db -> s -> db.getGames().values(),
                InflatedGame::getName,
                InflatedGame::getIcon,
                (inflatedGame, selector) -> selector.selectGame(inflatedGame.getName()),
                RegionalSelectorActivity.class
        ));

        sharpFiles = SharpFiles.setup(this);

        GameDb db = new GameDb();
        DataSelector selector = DataSelector.builder().build();

        db.rebuildGame(selector.selectGame("POWERUP"), g ->
                g.addFieldDefs(FieldDefinition.newBuilder()
                        .setName("TestString").setType(FieldDefinition.Type.STRING))
                        .putRegionals("Ventura", Regional.newBuilder()
                                .setName("Ventura")
                                .putMatches(1, Match.newBuilder()
                                        .setMatchNumber(1)
                                        .putTeams(5818, TeamMatch.newBuilder()
                                                .setTeamNumber(5818)
                                                .putValues("TestString", FieldValue.newBuilder().setStr("The Value, Yes!").build())
                                                .build())
                                        .build())
                                .build())
                        .build());

        db.rebuildGame(selector.selectGame("STEAMWORKS"), g -> {
        });
        db.rebuildGame(selector.selectGame("STRONGHOLD"), g -> {
        });

        sharpFiles.getSavedGameFiles().stream()
                .map(this::loadGame)
                .forEach(g ->
                        db.rebuildGame(selector.selectGame(g.getName()), b -> b.mergeFrom(g))
                );

        adapter.initialize(db, selector);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (messages == null) {
                return;
            }
            NdefMessage[] ndefs = (NdefMessage[]) messages;
            for (NdefMessage ndef : ndefs) {
                for (NdefRecord record : ndef.getRecords()) {
                    byte[] frameMessage = record.getPayload();
                    try {
                        Game imported = Game.parseFrom(frameMessage);
                        File file = sharpFiles.getSavedGameFile(imported.getName());
                        try (FileOutputStream stream = new FileOutputStream(file)) {
                            imported.writeTo(stream);
                        }
                    } catch (Exception e) {
                        Log.i("gameImporting", "Game failed to import", e);
                    }
                }
            }
        }
    }

    private Game loadGame(File source) {
        try (FileInputStream in = new FileInputStream(source)) {
            return Game.parseFrom(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
