package com.cooktogether.mainscreens;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cooktogether.helpers.ConversationListActivity;
import com.cooktogether.helpers.Localization;
import com.cooktogether.helpers.AbstractBaseActivity;
import com.cooktogether.R;

public class HomeActivity extends AbstractBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsConnected();

        setContentView(R.layout.activity_home);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_main));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.app_name);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    public void newMeal(View view) {
        Intent intent = new Intent(this, MealActivity.class);
        startActivity(intent);
    }

    public void searchMeal(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    public void goToLocalization(View view) {
        Intent intent = new Intent(this, Localization.class);
        startActivity(intent);
    }

    public void myMeals(View view) {
        Intent intent = new Intent(this, MyMealsActivity.class);
        startActivity(intent);
    }

    public void goToConversations(View view){
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
    }
}



