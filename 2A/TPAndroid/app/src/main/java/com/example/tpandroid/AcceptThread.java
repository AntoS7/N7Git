package com.example.tpandroid;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.bluetooth.*;
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

    private final Handler handler = new Handler(msg -> {
        switch (msg.what) {
            case 0: // MESSAGE_READ
                byte[] readBuf = (byte[]) msg.obj;
                String receivedMessage = new String(readBuf, 0, msg.arg1);
                // Met à jour l'UI ou traite le message
                break;
            case 1: // MESSAGE_WRITE
                // Message écrit
                break;
            case 2: // MESSAGE_TOAST
                // Affiche un toast d'erreur
                break;
        }
        return true;
    });
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public AcceptThread(String serv_name, UUID uuid) {
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();;
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