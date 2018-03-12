package org.rivierarobotics.sharpeyes;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.rivierarobotics.protos.Game;
import org.rivierarobotics.sharpeyes.adapters.ReceiveDeviceAdapter;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GameReceiveActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0xb1;

    private final ExecutorService threadPool = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("SharpEyes - BT Request %d").build()
    );

    private ReceiveDeviceAdapter adapter = new ReceiveDeviceAdapter(this);
    private RecyclerView deviceView;
    private GameDb db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_receive);

        deviceView = findViewById(R.id.listView);
        deviceView.setHasFixedSize(true);
        deviceView.setLayoutManager(new LinearLayoutManager(this));
        deviceView.setAdapter(adapter);

        db = GameDb.loadFrom(getIntent());
        TextView title = findViewById(R.id.title);
        title.setText(R.string.receive_title);

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
        Log.i("ReceiveViaBT", "Requesting bluetooth enablement...");
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
        }
    }

    private void postBluetoothInit() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()) {
            requestBluetoothEnabled();
            return;
        }

        Log.i("ReceiveViaBT", "Starting post-BT filtering...");
        registerReceiver(receiveDeviceFindAction, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        Thread t = new Thread(() -> {
            adapter.getBondedDevices().forEach(this::addItemIfMatches);

            if (!adapter.startDiscovery()) {
                Log.w("ReceiveViaBT", "Failed to start discovery process!");
            }
        }, getPackageName() + " - Bluetooth Client");
        t.start();
    }

    private final BroadcastReceiver receiveDeviceFindAction = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addItemIfMatches(device);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiveDeviceFindAction);
    }

    private void addItemIfMatches(BluetoothDevice device) {
        Log.d("ReceiveViaBT", "Checking device " + device.getName() + " for UUID");
        if (Stream.of(device.getUuids())
                .map(ParcelUuid::getUuid)
                .noneMatch(Predicate.isEqual(BTHelper.RFCOMM_ID))) {
            return;
        }
        runOnUiThread(() -> {
            int[] itemId = {0};
            itemId[0] = adapter.addItem(new ReceiveDeviceAdapter.Item(device.getName(), view -> getGameFromDevice(device, itemId[0])));
        });
    }

    private void getGameFromDevice(BluetoothDevice device, int item) {
        setStatusAsync(item, ReceiveDeviceAdapter.Item.Status.QUEUED);
        threadPool.submit(() -> {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            setStatusAsync(item, ReceiveDeviceAdapter.Item.Status.FETCHING);
            try (BluetoothSocket socket = device.createRfcommSocketToServiceRecord(BTHelper.RFCOMM_ID)) {
                DataInputStream stream = new DataInputStream(socket.getInputStream());
                int command = stream.readInt();
                if (command != BTHelper.COMMAND_RECEIVE_GAME) {
                    setStatusAsync(item, ReceiveDeviceAdapter.Item.Status.FAILED);
                    return;
                }

                Game game = Game.parseDelimitedFrom(stream);
                db.rebuildGame(DataSelector.builder().gameId(game.getName()).build(), b -> b.mergeFrom(game));
                setStatusAsync(item, ReceiveDeviceAdapter.Item.Status.COMPLETE);
            } catch (IOException e) {
                setStatusAsync(item, ReceiveDeviceAdapter.Item.Status.FAILED);
                Log.e("ReceiveViaBT", "Failed while fetching game", e);
            } finally {
                BluetoothAdapter.getDefaultAdapter().startDiscovery();
            }
        });
    }

    private void setStatusAsync(int item, ReceiveDeviceAdapter.Item.Status fetching) {
        runOnUiThread(() -> adapter.updateItemStatus(item, fetching));
    }
}
