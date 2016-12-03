package com.cooktogether.mainscreens;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cooktogether.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void newDate(View view) {
        Intent intent=new Intent(this, CreateActivity.class);
        startActivity(intent);
    }

    public void searchDate(View view) {
        Intent intent=new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
}



