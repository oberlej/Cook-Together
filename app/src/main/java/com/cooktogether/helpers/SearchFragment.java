package com.cooktogether.helpers;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.cooktogether.R;
import com.cooktogether.fragments.MyMealsFragment;
import com.cooktogether.mainscreens.HomeActivity;

/**
 * Created by hela on 08/01/17.
 */

public class SearchFragment extends AbstractBaseFragment implements OnClickListener {

    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView nvDrawer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search2, container, false);
        init(view);
        return view;
    }

    @Override
    protected void init(View view) {
        mParent = (HomeActivity) getActivity();
        view.findViewById(R.id.list_search_btn).setOnClickListener(this);
        view.findViewById(R.id.map_search_btn).setOnClickListener(this);
        /*// Set a Toolbar to replace the ActionBar.
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar_main);
        mParent.setSupportActionBar(mToolbar);
        mParent.getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Setup drawer
        mDrawer = (DrawerLayout) view.findViewById(R.id.search_drawer_layout);
        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);
        //Setup navigation view
        nvDrawer = (NavigationView) view.findViewById(R.id.search_options_menu);
        setupDrawerContent(nvDrawer);
        // Inflate the header view at runtime
        View headerLayout = nvDrawer.inflateHeaderView(R.layout.nav_header);*/
        loadDefaultScreen();
    }

    /*private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        onOptionsItemSelected(menuItem);
                        return true;
                    }
                });
    }
    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(mParent, mDrawer, mToolbar, R.string.drawer_open, R.string.drawer_close);
    }*/
    @Override
    public void onClick(View v) {
        Fragment fragment = null;
        Class fragmentClass;
        // Handle presses on the action menu items
        switch (v.getId()) {
            case R.id.list_search_btn:
                fragmentClass = com.cooktogether.fragments.SearchFragment.class;
                //return true;
                break;
            case R.id.map_search_btn:
                fragmentClass = LocalizationFragment.class;
                break;
            default:
                fragmentClass = SearchFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        showFragment(fragment);
        //return true;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_search_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;

    }

    private void showFragment(Fragment f) {
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = mParent.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.search_screen_content, f).commit();
    }
    public void loadDefaultScreen() {
        Fragment fragment = com.cooktogether.fragments.SearchFragment.newInstance();
        showFragment(fragment);
    }
}
