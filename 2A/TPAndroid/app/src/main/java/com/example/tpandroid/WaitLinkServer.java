package com.example.tpandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

// Nouveaux imports pour la récupération des appareils appairés
import java.util.Set;
import android.bluetooth.BluetoothDevice;

import android.util.Log;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

public class WaitLinkServer extends AppCompatActivity {

    private UUID mon_UUID = UUID.fromString("8e9246e1-fb3d-4f68-bd2b-5e1e21bcb4b6");
    private String nom_serv = "MonServeur";
    private AcceptThread acceptThread;
    // Handler partagé pour les threads de communication
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;

    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static final int REQUEST_ENABLE_BT = 1;

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
        if (adapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
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

        acceptThread = new AcceptThread(
                bluetoothAdapter,
                nom_serv,
                mon_UUID,
                handler
        );
        Log.d(TAG, "onCreate: Starting AcceptThread (service=" + nom_serv + ", uuid=" + mon_UUID + ")");
        acceptThread.start();
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
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private final Handler handler;
        private final BluetoothAdapter adapter;

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
    }
    /**
     * Called when a connection has been accepted.
     * Here you’d spin off a thread to do the actual read/write.
     */
    private void manageMyConnectedSocket(BluetoothSocket socket) {
        new ConnectedThread(socket, handler).start();
    }

    private static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream
        private Handler handler;

        public ConnectedThread(BluetoothSocket socket, Handler handler) {
            this.mmSocket = socket;
            this.handler = handler;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Initialize streams
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
            Log.d(TAG, "ConnectedThread: input and output streams initialized");
        }

        public void run() {
            Log.d(TAG, "ConnectedThread run: start listening for incoming data");
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    Log.d(TAG, "ConnectedThread run: read " + numBytes + " bytes");
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                Log.d(TAG, "ConnectedThread write: wrote " + bytes.length + " bytes");

                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
    /** Shared message codes */
    private static class MessageConstants {
        static final int MESSAGE_READ  = 0;
        static final int MESSAGE_WRITE = 1;
        static final int MESSAGE_TOAST = 2;
    }
}