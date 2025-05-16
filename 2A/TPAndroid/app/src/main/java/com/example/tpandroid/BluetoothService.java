package com.example.tpandroid;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Service;
import android.bluetooth.*;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import java.util.UUID;

public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    private final IBinder binder = new LocalBinder();
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;
    private Handler handler;

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BluetoothService onCreate: called");
        handler = new Handler();
        BluetoothManager mgr = getSystemService(BluetoothManager.class);
        BluetoothAdapter adapter = mgr.getAdapter();
        if (adapter != null) {
            // Démarre l'AcceptThread et redirige la socket vers onDeviceConnected()
            acceptThread = new AcceptThread(
                    adapter,
                    "MonServeur",
                    UUID.fromString("8e9246e1-fb3d-4f68-bd2b-5e1e21bcb4b6"),
                    handler
            ) {
                @Override
                protected void onDeviceConnected(BluetoothSocket socket) {
                    BluetoothService.this.onDeviceConnected(socket);
                }
            };
            acceptThread.start();
            Log.d(TAG, "BluetoothService onCreate: acceptThread started");
        } else {
            Log.e(TAG, "Bluetooth non supporté");
        }
    }

    public void onDeviceConnected(BluetoothSocket socket) {
        connectedThread = new ConnectedThread(socket, handler);
        connectedThread.start();
        // Launch SmartHouse activity when Bluetooth connection is established
        Intent intent = new Intent(getApplicationContext(), SmartHouse.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /** Pour envoyer des octets depuis n’importe quelle Activity bindée. */
    public void write(byte[] data) {
        if (connectedThread != null) {
            connectedThread.write(data);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        if (acceptThread != null) acceptThread.cancel();
        if (connectedThread != null) connectedThread.cancel();
        super.onDestroy();
    }
}