package com.cooktogether.mainscreens;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.fragments.ChatFragment;
import com.cooktogether.fragments.ChatListFragment;
import com.cooktogether.fragments.MealNotEditableFragment;
import com.cooktogether.fragments.MyMealFragment;
import com.cooktogether.fragments.MyMealsFragment;
import com.cooktogether.fragments.MyMealsListFragment;
import com.cooktogether.fragments.MyReservationsFragment;
import com.cooktogether.fragments.ProfileFragment;
import com.cooktogether.helpers.AbstractBaseActivity;
import com.cooktogether.helpers.SearchFragment;
import com.cooktogether.helpers.UploadPicture;
import com.cooktogether.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AbstractBaseActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION_ACCESS = 1333;
    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA = 1293;
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;

    private NavigationView nvDrawer;

    private ActionBarDrawerToggle drawerToggle;

    public String mMealKey = "";
    private String conversationKey;

    private MenuItem itemChecked = null;

    private User mUser = null;
    private UploadPicture picLoader;
    private User toVisit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsConnected();

        setContentView(R.layout.drawer_main);
        init();

        //set default item
        loadDefaultScreen();
        checkPermissions();
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //show an explanation

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION_ACCESS);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //show an explanation

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_MEDIA:{
                return;
            }
        }
    }

    @Override
    protected void init() {
        // Set a Toolbar to replace the ActionBar.
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.app_name));

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
        CircleImageView ivHeaderPhoto = (CircleImageView) headerLayout.findViewById(R.id.profile_picture_view);
        ivHeaderPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfile();
            }
        });
        TextView mUserName = (TextView) headerLayout.findViewById(R.id.user_name_view);

        loadUser(ivHeaderPhoto, mUserName);
        setMenuCounterListener();
    }

    private void setMenuCounterListener() {
        getDB().child(getString(R.string.db_users)).child(getUid()).child(getString(R.string.db_unread)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null || !dataSnapshot.exists()) {
                    return;
                }
                int unread = ((Long) dataSnapshot.getValue()).intValue();
                setMenuCounter(R.id.nav_my_messages, unread);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, R.string.fail_load_profile, Toast.LENGTH_LONG).show();
            }
        });
    }

    //Todo use the same function as in the profile fragment
    private void loadUser(final CircleImageView userPic, final TextView mUserName) {

        if (mUser == null) {
            getDB().child(getString(R.string.db_users)).child(getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        Toast.makeText(HomeActivity.this, getString(R.string.fail_load_profile), Toast.LENGTH_LONG).show();
                        loadDefaultScreen();
                        return;
                    }
                    mUser = User.parseSnapshot(dataSnapshot);
                    mUserName.setText(mUser.getUserName());
                    picLoader = new UploadPicture(HomeActivity.this, mUser, userPic, getCurrentUser(), getRootRef(), getDB());
                    //profile pic
                    loadPicture();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(HomeActivity.this, getString(R.string.fail_load_profile), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            mUserName.setText(mUser.getUserName());
            //profile pic
            picLoader = new UploadPicture(HomeActivity.this, mUser, userPic, getCurrentUser(), getRootRef(), getDB());
            loadPicture();

        }
    }

    public void loadPicture() {
        picLoader.loadPicture();
    }

    public void resetPicture() {
        picLoader.resetPicture();
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
                fragmentClass = MyMealsListFragment.class;
                break;
            case R.id.nav_meal_detail:
                fragmentClass = MyMealFragment.class;
                break;
            case R.id.nav_search_meal:
                fragmentClass = SearchFragment.class;
                break;
            case R.id.nav_my_reservations:
                fragmentClass = MyReservationsFragment.class;
                break;
            case R.id.nav_my_messages:
                fragmentClass = ChatListFragment.class;
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
        Fragment f = ChatFragment.newInstance();
        showFragment(f);
        //itemChecked.setChecked(false);
        //itemChecked = null;
        mDrawer.closeDrawers();

    }

    public void goToMeal(String mealKey) {
        setMealKey(mealKey);
        Fragment f = MealNotEditableFragment.newInstance();
        showFragment(f);
        //itemChecked.setChecked(false);
        //itemChecked = null;
        mDrawer.closeDrawers();
    }

    public void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void setMenuCounter(@IdRes int itemId, int count) {
        TextView view = (TextView) nvDrawer.getMenu().findItem(itemId).getActionView();
        view.setText(count > 0 ? String.valueOf(count) : null);
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User mUser) {
        this.mUser = mUser;
    }

    public void setToVisit(User toVisit) {
        this.toVisit = toVisit;
    }

    public User getToVisit() {
        return toVisit;
    }
}



