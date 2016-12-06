package com.cooktogether;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }
    //-- MAP !!!
    public void goToMap(View view){
        Intent intent = new Intent(this,Map.class);
        startActivity(intent);
    }

    public void goToLocalization(View view){
        Intent intent = new Intent(this,Localization.class);
        startActivity(intent);
    }

    //----
}
