package org.rivierarobotics.sharpeyes;

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.sharpeyes.adapters.InflatedGame;

import java.io.DataOutputStream;
import java.io.IOException;

import static org.rivierarobotics.sharpeyes.BTHelper.RFCOMM_ID;

public class GameSendActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0xb1;
    private static final int REQUEST_DISCOVER_BT = 0xb1d;

    private ImageView blueIcon;
    private View wrapper;
    private Game gameToWrite;
    private volatile boolean visible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_send);

        blueIcon = findViewById(R.id.blueIcon);
        Animation infinirot = AnimationUtils.loadAnimation(this, R.anim.infinirot);
        infinirot.setFillAfter(true);
        infinirot.setInterpolator(new LinearInterpolator());
        blueIcon.startAnimation(infinirot);


        wrapper = findViewById(R.id.blueIconWrapper);

        GameDb db = GameDb.loadFrom(getIntent());
        DataSelector selector = DataSelector.loadFrom(getIntent());
        TextView title = findViewById(R.id.title);
        InflatedGame game = db.getGame(selector);
        title.setText(game.getName());
        ImageView image = findViewById(R.id.image);
        if (game.getIcon() != null) {
            image.setImageBitmap(game.getIcon());
        }

        gameToWrite = game.getBase();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.no_bluetooth, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            requestBluetoothEnabled();
            return;
        }
        postBluetoothInit();
    }

    private void requestBluetoothEnabled() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.no_bluetooth, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            postBluetoothInit();
        } else if (requestCode == REQUEST_DISCOVER_BT) {
            postDiscoverable();
        }
    }

    private void postBluetoothInit() {
        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), REQUEST_DISCOVER_BT);
    }

    private void postDiscoverable() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        Thread t = new Thread(() -> {
            try (BluetoothServerSocket serverSocket = adapter.listenUsingRfcommWithServiceRecord("SharpEyes", RFCOMM_ID)) {
                while (visible) {
                    BluetoothSocket socket = serverSocket.accept();
                    DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
                    stream.writeInt(BTHelper.COMMAND_RECEIVE_GAME);
                    gameToWrite.writeDelimitedTo(stream);
                    runOnUiThread(() -> {
                        ObjectAnimator toBlueAnimator = ObjectAnimator.ofArgb(wrapper, "backgroundColor", getColor(R.color.bluetooth));
                        toBlueAnimator.setDuration(250);
                        toBlueAnimator.setRepeatCount(ObjectAnimator.INFINITE);
                        toBlueAnimator.setRepeatMode(ObjectAnimator.REVERSE);
                        toBlueAnimator.start();
                    });
                }
            } catch (IOException e) {
                Log.e("SendViaBT", "Error while sending", e);
            }
        }, getPackageName() + " - Bluetooth Server");
        t.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        visible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        visible = false;
    }
}
