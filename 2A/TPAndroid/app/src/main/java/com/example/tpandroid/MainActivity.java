package com.example.tpandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.accueil), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Boutton de lancement du client
        Button btn_cli = (Button) findViewById(R.id.BT_S_C);
        btn_cli.setOnClickListener(this);

        //Boutton de lancement du serveur
        Button btn_serv = (Button) findViewById(R.id.BT_S_S);
        btn_serv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.BT_S_C) {
            Button btn = (Button) findViewById(R.id.BT_S_C);
            btn.setText("Lancement du client !");
            Intent playIntent = new Intent(this, SmartHouse.class);
            startActivity(playIntent);
        }
        if (v.getId()==R.id.BT_S_S) {
            Button btn = (Button) findViewById(R.id.BT_S_C);
            btn.setText("Serveur en attente");
            Intent playIntent = new Intent(this, WaitLinkServer.class);
            startActivity(playIntent);
        }

    }
}