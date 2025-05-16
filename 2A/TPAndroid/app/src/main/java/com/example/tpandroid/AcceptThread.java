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

public abstract class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    private final Handler handler;
    private final BluetoothAdapter adapter;

    private UUID mon_UUID = UUID.fromString("8e9246e1-fb3d-4f68-bd2b-5e1e21bcb4b6");
    private String nom_serv = "MonServeur";

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public AcceptThread(BluetoothAdapter bluetoothAdapter,
                        String serviceName,
                        UUID serviceUuid,
                        Handler handler) {
        this.adapter = bluetoothAdapter;
        this.handler = handler;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(nom_serv, mon_UUID);
            Log.d(TAG, "AcceptThread: server socket created for serviceName="
                    + serviceName + " uuid=" + serviceUuid);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void run() {
        Log.d(TAG, "AcceptThread run: waiting for connections");
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
                Log.d(TAG, "AcceptThread run: accept() returned socket=" + socket);
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // Log details about the remote device
                BluetoothDevice remoteDevice = socket.getRemoteDevice();
                Log.d(TAG, "Connection established with device: "
                        + remoteDevice.getName() + " [" + remoteDevice.getAddress() + "]");
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(socket);

                Log.d(TAG, "AcceptThread run: connection accepted, closing server socket");
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
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

    protected abstract void onDeviceConnected(BluetoothSocket socket);

    /**
     * Called when a connection has been accepted.
     * Here you’d spin off a thread to do the actual read/write.
     */
    private void manageMyConnectedSocket(BluetoothSocket socket) {
        onDeviceConnected(socket);
    }

}