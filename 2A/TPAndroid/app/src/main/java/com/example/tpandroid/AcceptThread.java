package com.example.tpandroid;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.bluetooth.*;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.UUID;

public class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;

    private MyBluetoothService.ConnectedThread connectedThread;

    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public AcceptThread(BluetoothAdapter bluetoothAdapter, String serv_name, UUID uuid, Handler handler) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.handler = handler;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(serv_name, uuid);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                connectedThread = new MyBluetoothService().new ConnectedThread(socket);
                // TODO: Ajouter un mécanisme pour transmettre le handler à ConnectedThread si nécessaire
                connectedThread.start();
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close the connect socket", e);
                }
                break;
            }
        }
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}