package org.rivierarobotics.sharpeyes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.sharpeyes.adapters.SelectorAdapter;
import org.rivierarobotics.sharpeyes.adapters.InflatedGame;
import org.rivierarobotics.sharpeyes.gamedb.GameDb;
import org.rivierarobotics.sharpeyes.gamedb.GameDbAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class GameSelectorActivity extends AppCompatActivity {

    private SharpFiles sharpFiles;
    private SelectorAdapter<InflatedGame> adapter;
    private RecyclerView gamesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_game_select);

        gamesView = findViewById(R.id.listView);
        gamesView.setHasFixedSize(true);
        gamesView.setLayoutManager(new LinearLayoutManager(this));
        gamesView.setAdapter(adapter = new SelectorAdapter<>(
                this,
                Comparator.comparing(InflatedGame::getName),
                db -> s -> db.getGames().values(),
                InflatedGame::getName,
                InflatedGame::getIcon,
                (inflatedGame, selector) -> selector.selectGame(inflatedGame.getName()),
                null,
                RegionalSelectorActivity.class
        ));

        GameDb.initialize(getApplicationContext());

        verifyStoragePermissions();

    }

    private void postPermInit() {
        sharpFiles = SharpFiles.setup(this);

        GameDbAccess.initialize(sharpFiles, GameDb.getInstance().getDao());
        GameDbAccess db = GameDbAccess.getInstance();
        DataSelector selector = DataSelector.builder().build();
//
//        db.rebuildGame(selector.selectGame("POWERUP"), g ->
//                g.addFieldDefs(FieldDefinition.newBuilder()
//                        .setName("TestString").setType(FieldDefinition.Type.STRING))
//                        .putRegionals("Ventura", Regional.newBuilder()
//                                .setName("Ventura")
//                                .putMatches(1, Match.newBuilder()
//                                        .setMatchNumber(1)
//                                        .putTeams(5818, TeamMatch.newBuilder()
//                                                .setTeamNumber(5818)
//                                                .putValues("TestString", FieldValue.newBuilder().setStr("The Value, Yes!").build())
//                                                .build())
//                                        .build())
//                                .build())
//                        .build());
//
//        db.rebuildGame(selector.selectGame("STEAMWORKS"), g -> {
//        });
//        db.rebuildGame(selector.selectGame("STRONGHOLD"), g -> {
//        });

        sharpFiles.getSavedGameFiles().stream()
                .map(this::loadGame)
                .forEach(g ->
                        db.getGames().put(g.getName(), InflatedGame.inflate(g))
                );

        Thread task = new Thread(() -> {
            GameDb.getInstance().getDao().getAll()
                    .forEach(g ->
                            db.rebuildGame(selector.selectGame(g.getName()), b -> {
                                if (g.getGame() != null) {
                                    return g.getGame().toBuilder();
                                }
                                return b;
                            })
                    );
            runOnUiThread(adapter::onReloadRequest);
        }, "db-task");
        task.start();

        adapter.initialize(db, selector);


        Button sendButton = findViewById(R.id.receiveButton);
        sendButton.setOnClickListener(view -> {
            AndroidUtil.startActivityForResult(this, SelectorAdapter.REQUEST_CODE, GameReceiveActivity.class,
                    intent -> {
                        adapter.getSelector().saveTo(intent);
                    });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SelectorAdapter.REQUEST_CODE) {
            adapter.onReloadRequest();
        }
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            return;
        }
        postPermInit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length == 0) {
                Toast.makeText(this, R.string.no_permission, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            for (int grantResult : grantResults) {
                if (grantResult == PERMISSION_DENIED) {
                    Toast.makeText(this, R.string.no_permission, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
        }
        postPermInit();
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
