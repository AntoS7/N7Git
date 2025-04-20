package com.example.tpandroid;


import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SmartHouse extends AppCompatActivity {

    private LinearLayout myLayout;

    private final String URL = "http://happyresto.enseeiht.fr/smartHouse/api/v1/devices/32";
    private final String HOUSE_ID = "32";

    private Handler handler;
    private Runnable runnableCode;

    protected View createDeviceView(String id, String brand, String name, String autonomy, String data, Boolean state){
        RelativeLayout layout = new RelativeLayout ( this ) ;

        //Texte en haut à gauche
        RelativeLayout.LayoutParams paramsTopLeft =
                new RelativeLayout . LayoutParams (
                        RelativeLayout . LayoutParams .WRAP_CONTENT,
                        RelativeLayout . LayoutParams .WRAP_CONTENT) ;
        paramsTopLeft . addRule ( RelativeLayout .ALIGN_PARENT_LEFT,
                RelativeLayout .TRUE) ;
        paramsTopLeft . addRule ( RelativeLayout .ALIGN_PARENT_TOP,
                RelativeLayout .TRUE) ;
        TextView someTextView = new TextView ( this ) ;
        someTextView.setText (String.format("[%s]%s", brand, name));
        layout.addView (someTextView , paramsTopLeft ) ;

        //Texte en bas à gauche
        RelativeLayout.LayoutParams paramsBottomLeft =
                new RelativeLayout . LayoutParams (
                        RelativeLayout . LayoutParams .WRAP_CONTENT,
                        RelativeLayout . LayoutParams .WRAP_CONTENT) ;
        paramsBottomLeft . addRule ( RelativeLayout .ALIGN_PARENT_LEFT,
                RelativeLayout .TRUE) ;
        paramsBottomLeft . addRule ( RelativeLayout .ALIGN_PARENT_BOTTOM,
                RelativeLayout .TRUE) ;
        TextView someotherTextView = new TextView ( this ) ;
        String TextContent;
        if (!Objects.equals(autonomy, "-1")) {
            TextContent = "Autonomy : " + autonomy + " Data : " + data;
        }
        else {
            TextContent = " Data : " + data;
        }
        someotherTextView.setText (TextContent);
        layout.addView (someotherTextView , paramsBottomLeft ) ;

        //Bouton à droite
        RelativeLayout.LayoutParams paramsRight =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        paramsRight.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        Button stateButton = new Button(this);

        stateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                String currentState = button.getText().toString();
                String newState = "ON".equals(currentState) ? "0" : "1";
                button.setText("ON".equals(currentState) ? "OFF" : "ON");

                Log.d(TAG, "Appareil " + ("ON".equals(currentState) ? "éteint" : "allumé"));

                RequestQueue queue = Volley.newRequestQueue(SmartHouse.this);
                String url = "http://happyresto.enseeiht.fr/smartHouse/api/v1/devices/";

                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        response -> Log.d(TAG, "Action envoyée au serveur"),
                        error -> {
                            Log.e(TAG, "Erreur lors de la requête POST", error);
                            Toast.makeText(SmartHouse.this, "Erreur de mise à jour", Toast.LENGTH_SHORT).show();
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("deviceId", id);
                        params.put("houseId", HOUSE_ID);
                        params.put("action", "turnOnOff");
                        return params;
                    }
                };
            }
        });
        if (state) {
            stateButton.setText("ON");
        } else {
            stateButton.setText("OFF");
        }
        layout.addView(stateButton, paramsRight);

        return layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_smart_house);
        myLayout = (LinearLayout) findViewById(R.id.ll);

        handler = new Handler();
        runnableCode = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Mise à jour périodique des appareils");

                RequestQueue queue = Volley.newRequestQueue(SmartHouse.this);
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET, URL, null,
                    requestSuccessListener(), volleyErrorListener()
                );
                queue.add(jsonArrayRequest);

                handler.postDelayed(this, 10000); // Réexécute toutes les 10 secondes
            }
        };
        //handler.postDelayed(runnableCode, 0);
    }

    private Response.Listener<JSONArray> requestSuccessListener() {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject device = response.getJSONObject(i);

                        String id = device.getString("ID");
                        String brand = device.getString("BRAND");
                        String name = device.getString("NAME");
                        String autonomy = device.getString("AUTONOMY");
                        String data = device.getString("DATA");
                        boolean state = device.getInt("STATE") == 1;

                        View deviceView = createDeviceView(id, brand, name, autonomy, data, state);
                        myLayout.addView(deviceView);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Erreur de parsing JSON", e);
                }
            }
        };
    }
    private Response.ErrorListener volleyErrorListener(){
        return new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.getMessage());
            }
        } ;
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null && runnableCode != null) {
            handler.removeCallbacks(runnableCode);
            Log.d(TAG, "Tâches périodiques arrêtées dans onPause");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (handler != null && runnableCode != null) {
            handler.postDelayed(runnableCode, 0);
            Log.d(TAG, "Tâches périodiques relancées dans onResume");
        }
    }
}