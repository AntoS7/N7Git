package com.example.tpandroid;


import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.*;
import com.android.volley.toolbox.Volley;


import org.json.JSONObject;

public class SmartHouse extends AppCompatActivity {

    private LinearLayout myLayout = (LinearLayout) findViewById(R.id.ll);

    private final String BASE_URL = "http://happyresto.enseeiht.fr/smartHouse/api/v1/";
    private final String HOUSE_ID = "32";

    protected View createDeviceView(String name, String data, Boolean state){
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
        someTextView.setText (name);
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
        someotherTextView.setText (data);
        layout.addView (someotherTextView , paramsBottomLeft ) ;

        //Texte à droite
        RelativeLayout.LayoutParams paramsRight =
                new RelativeLayout . LayoutParams (
                        RelativeLayout . LayoutParams .WRAP_CONTENT,
                        RelativeLayout . LayoutParams .WRAP_CONTENT) ;
        paramsBottomLeft . addRule ( RelativeLayout .ALIGN_PARENT_RIGHT,
                RelativeLayout .TRUE) ;
        TextView somETextView = new TextView ( this ) ;
        if (state == true){
            somETextView.setText("ON");
        }
        else {
            somETextView.setText("OFF");
        }
        layout.addView (somETextView , paramsRight ) ;

        return layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_smart_house);

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, BASE_URL , null ,
                            requestSuccessListener() , requestErrorListener( ) ) ;
            queue.add(jsonObjectRequest) ;
        }
        private Response.Listener <JSONObject> requestSuccessListener() {
            return new Response.Listener <JSONObject >() {
                @Override
                public void onResponse (JSONObject response) {
                    Log.d(TAG,response.toString());
                }
            } ;
        }
        private Response.ErrorListener volleyErrorListener(){
            return new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG,error.getMessage());
                }
            } ;
        }

        /*setContentView(R.layout.activity_smart_house);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.house), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        myLayout.addView(createDeviceView("Test1","data",true));
        */
    }


}