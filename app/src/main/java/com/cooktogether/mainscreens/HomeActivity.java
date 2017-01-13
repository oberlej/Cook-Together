package com.cooktogether.mainscreens;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.cooktogether.fragments.MealNotEditableFragment;
import com.cooktogether.fragments.ConversationFragment;

import com.cooktogether.fragments.MyMealFragment;
import com.cooktogether.fragments.MyMealsFragment;
import com.cooktogether.fragments.ProfileFragment;

import com.cooktogether.helpers.AbstractBaseActivity;
import com.cooktogether.R;
import com.cooktogether.fragments.ConversationsListFragment;

public class HomeActivity extends AbstractBaseActivity {

    private DrawerLayout mDrawer;
    private Toolbar mToolbar;

    private NavigationView nvDrawer;

    private ActionBarDrawerToggle drawerToggle;

    private String mMealKey = "";
    private String conversationKey;

    private MenuItem itemChecked = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsConnected();

        setContentView(R.layout.drawer_main);
        init();


        //set default item
        loadDefaultScreen();
    }

    @Override
    protected void init() {
        // Set a Toolbar to replace the ActionBar.
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Setup drawer
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);
        //Setup navigation view
        nvDrawer = (NavigationView) findViewById(R.id.drawer_nav_view);
        setupDrawerContent(nvDrawer);
        // Inflate the header view at runtime
        View headerLayout = nvDrawer.inflateHeaderView(R.layout.nav_header);
        // We can now look up items within the header if needed
//        ImageView ivHeaderPhoto = headerLayout.findViewById(R.id.imageView);
    }

    public void loadDefaultScreen() {
        MenuItem defaultItem = nvDrawer.getMenu().findItem(R.id.nav_my_meals);
        selectDrawerItem(defaultItem, defaultItem.getTitle().toString());
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        };
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem, menuItem.getTitle().toString());
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem, String title) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        itemChecked = menuItem;
        switch (menuItem.getItemId()) {
            case R.id.nav_my_meals:
                fragmentClass = MyMealsFragment.class;
                break;
            case R.id.nav_meal_detail:
                fragmentClass = MyMealFragment.class;
                break;
            case R.id.nav_search_meal:
                fragmentClass = com.cooktogether.helpers.SearchFragment.class;
                break;
            case R.id.nav_my_messages:
                fragmentClass = ConversationsListFragment.class;
                break;
            case R.id.nav_logout:
                logout();
                return;
            default:
                fragmentClass = MyMealsFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        showFragment(fragment);

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(title);
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    private void showFragment(Fragment f) {
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.drawer_screen_content, f).commit();
    }

    public void showProfile() {
        Fragment f = ProfileFragment.newInstance();
        showFragment(f);
        itemChecked.setChecked(false);
        itemChecked = null;
        setTitle("Profile");
        mDrawer.closeDrawers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_profile:
                showProfile();
                return true;
            case R.id.action_message:
                goToConversations();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getMealKey() {
        return mMealKey;
    }

    public void setMealKey(String mMealKey) {
        this.mMealKey = mMealKey;
    }

    public NavigationView getNvDrawer() {
        return nvDrawer;
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE 1: Make sure to override the method with only a single `Bundle` argument
    // Note 2: Make sure you implement the correct `onPostCreate(Bundle savedInstanceState)` method.
    // There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public void goToConversations() {
        selectDrawerItem(getNvDrawer().getMenu().findItem(R.id.nav_my_messages), getString(R.string.CONVERSATION_TITLE));
    }

    public void setConversationKey(String conversationKey) {
        this.conversationKey = conversationKey;
    }

    public String getConversationKey() {
        return conversationKey;
    }

    public void goToConversation(String conversationKey) {
        setConversationKey(conversationKey);
        Fragment f = ConversationFragment.newInstance();
        showFragment(f);
        //itemChecked.setChecked(false);
        //itemChecked = null;
        setTitle("Conversation");
        mDrawer.closeDrawers();

    }

    public void goToMeal(String mealKey) {
        setMealKey(mealKey);
        Fragment f = MealNotEditableFragment.newInstance();
        showFragment(f);
        //itemChecked.setChecked(false);
        //itemChecked = null;
        setTitle("Proposed Meal");
        mDrawer.closeDrawers();
    }
}



