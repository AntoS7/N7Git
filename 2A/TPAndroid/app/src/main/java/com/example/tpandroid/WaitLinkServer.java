package com.example.tpandroid;

import android.Manifest;
import android.bluetooth.BluetoothServerSocket;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.UUID;

public class WaitLinkServer extends AppCompatActivity {

    private UUID mon_UUID = UUID.fromString("8e9246e1-fb3d-4f68-bd2b-5e1e21bcb4b6");

    private String nom_serv = "MonServeur";

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wait_link_server);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Lancement de la connexion bluetooth
        AcceptThread acceptThread = new AcceptThread(nom_serv,mon_UUID);
        // TODO: Ajouter un mécanisme pour transmettre le handler à AcceptThread
        acceptThread.start();


    }


}