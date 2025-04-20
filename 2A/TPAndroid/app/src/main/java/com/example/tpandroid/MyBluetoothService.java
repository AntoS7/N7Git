package com.example.tpandroid;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyBluetoothService {

    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler handler; // Handler qui reçoit les infos du service Bluetooth

    // Définit plusieurs constantes utilisées lors de la transmission de messages
    // entre le service et l'UI.
    private interface MessageConstants {
        int MESSAGE_READ = 0;
        int MESSAGE_WRITE = 1;
        int MESSAGE_TOAST = 2;
        // ... (Ajoutez d'autres types de messages ici si nécessaire.)
    }

    class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer pour stocker le flux

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Obtient les flux d'entrée et de sortie ; utilise des objets temporaires car
            // les flux membres sont finals.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de la création du flux d'entrée", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de la création du flux de sortie", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // Octets retournés par read()

            // Continue d'écouter l'InputStream jusqu'à ce qu'une exception se produise.
            while (true) {
                try {
                    // Lit depuis l'InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Envoie les octets obtenus à l'activité de l'UI.
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Le flux d'entrée a été déconnecté", e);
                    break;
                }
            }
        }

        // Appelé depuis l'activité principale pour envoyer des données à l'appareil distant.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Partage le message envoyé avec l'activité de l'UI.
                Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de l'envoi des données", e);

                // Envoie un message d'échec à l'activité.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Impossible d'envoyer des données à l'autre appareil");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Appelé depuis l'activité principale pour fermer la connexion.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Impossible de fermer le socket de connexion", e);
            }
        }
    }
}