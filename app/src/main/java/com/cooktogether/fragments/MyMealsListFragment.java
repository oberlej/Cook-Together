package com.cooktogether.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooktogether.R;
import com.cooktogether.helpers.AbstractBaseFragment;
import com.cooktogether.helpers.ViewPagerAdapter;
import com.cooktogether.listener.OnBackPressListener;
import com.cooktogether.mainscreens.HomeActivity;

/**
 * Created by hela on 21/01/17.
 */

public class MyMealsListFragment extends AbstractBaseFragment {
    private Fragment mCurrent;
    private Fragment mBooked;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private TabLayout mTabLayout;

    private View v = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            v = inflater.inflate(R.layout.fragment_my_meals, container, false);

            init(v);
        return v;
    }

    @Override
    protected void init(View view) {
        mParent = (HomeActivity) getActivity();
        mParent.getSupportActionBar().setTitle("My Meals");
        ((HomeActivity) mParent).hideKeyboard(getContext());

        //init fragments
        mCurrent = MyMealsFragment.newInstance();
        mBooked = MyMealsFragment.newInstance();

        Bundle args = new Bundle();
        args.putBoolean("current", true);
        mCurrent.setArguments(args);

        Bundle args2 = new Bundle();
        args2.putBoolean("current", false);
        mBooked.setArguments(args2);
        //init view pager
        viewPager = (ViewPager) view.findViewById(R.id.my_meals_viewpager);
        setupViewPager(viewPager);
        adapter = ((ViewPagerAdapter) viewPager.getAdapter());

        //init tabLayout
        mTabLayout = (TabLayout) view.findViewById(R.id.my_meals_tabs);
        mTabLayout.setupWithViewPager(viewPager);

        loadDefaultScreen();
    }

    private void setupViewPager(final ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPager.setOffscreenPageLimit(1);
        adapter.addFragment(mCurrent, "Current");
        adapter.addFragment(mBooked, "Booked");

        viewPager.setAdapter(adapter);
    }

    public void showListSearch() {
        if (viewPager.getCurrentItem() != 0) viewPager.setCurrentItem(0);
    }

    public void showMapSearch() {
        if (viewPager.getCurrentItem() != 1) viewPager.setCurrentItem(1);
    }

    public void loadDefaultScreen() {
        showListSearch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public boolean onBackPressed() {
        // currently visible tab Fragment
        OnBackPressListener currentFragment = (OnBackPressListener) adapter.getItem(viewPager.getCurrentItem());

        if (currentFragment != null) {
            //see if the currentFragment or any of its childFragment can handle onBackPressed
            return currentFragment.onBackPressed();
        }

        // this Fragment couldn't handle the onBackPressed call
        return false;
    }
}
