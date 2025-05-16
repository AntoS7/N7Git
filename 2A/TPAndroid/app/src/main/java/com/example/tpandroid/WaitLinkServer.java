package com.example.tpandroid;

import android.Manifest;
import android.bluetooth.*;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.bluetooth.BluetoothDevice;

import android.util.Log;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import com.example.tpandroid.BluetoothService;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Set;


public class WaitLinkServer extends AppCompatActivity {

    // Handler partagé pour les threads de communication
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;

    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothService btService;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            btService = ((BluetoothService.LocalBinder)binder).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enregistrement du BroadcastReceiver pour ACTION_FOUND
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        Log.d(TAG, "onCreate: called");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wait_link_server);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation Bluetooth via BluetoothManager
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        this.bluetoothAdapter = adapter;
        Log.d(TAG, "onCreate: BluetoothAdapter = " + adapter
                + ", enabled? " + adapter.isEnabled());

        // Demande d'activation si nécessaire
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // --- Récupération des appareils déjà appairés ---
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();  // adresse MAC
                Log.d(TAG, "Appareil appairé: " + deviceName + " [" + deviceAddress + "]");
            }
        }

        // --- Création du Handler pour AcceptThread ---
        handler = new Handler(Looper.getMainLooper(), msg -> {
            switch (msg.what) {
                case MessageConstants.MESSAGE_READ:
                    byte[] buf = (byte[]) msg.obj;
                    String text = new String(buf, 0, msg.arg1);
                    Toast.makeText(this, "Received: " + text, Toast.LENGTH_SHORT).show();
                    break;
                case MessageConstants.MESSAGE_TOAST:
                    Toast.makeText(this, msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        });

        // Start and bind BluetoothService instead of AcceptThread
        Intent svcIntent = new Intent(this, BluetoothService.class);
        svcIntent.setAction("com.example.tpandroid.ACTION_START");
        Log.d(TAG, "Envoi de l’Intent vers le service: " + svcIntent);
        startService(svcIntent);
        bindService(svcIntent, serviceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "onCreate: BluetoothService started and bound");
    }

    // BroadcastReceiver pour découvrir de nouveaux appareils
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // adresse MAC
                Log.d(TAG, "Found device: " + deviceName + " [" + deviceHardwareAddress + "]");
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Désenregistrement du BroadcastReceiver
        unregisterReceiver(receiver);
        if (btService != null) {
            unbindService(serviceConnection);
        }
    }

    /**
     * Called when a connection has been accepted.
     * Here you’d spin off a thread to do the actual read/write.
     */
    private void manageMyConnectedSocket(BluetoothSocket socket) {
        new ConnectedThread(socket, handler).start();
    }


    /** Shared message codes */
    static class MessageConstants {
        static final int MESSAGE_READ  = 0;
        static final int MESSAGE_WRITE = 1;
        static final int MESSAGE_TOAST = 2;
    }
}