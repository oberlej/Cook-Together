package com.cooktogether.mainscreens;

import android.os.Bundle;

import com.cooktogether.helpers.AbstractBaseActivity;
import com.cooktogether.R;

public class SearchActivity extends AbstractBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsConnected();
        setContentView(R.layout.activity_search);
    }
}
