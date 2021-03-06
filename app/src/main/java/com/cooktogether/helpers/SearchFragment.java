package com.cooktogether.helpers;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooktogether.R;
import com.cooktogether.fragments.ListSearchFragment;
import com.cooktogether.fragments.MapSearchFragment;
import com.cooktogether.listener.OnBackPressListener;
import com.cooktogether.mainscreens.HomeActivity;

/**
 * Created by hela on 08/01/17.
 */

public class SearchFragment extends AbstractBaseFragment {

    private Fragment mList;
    private Fragment mMap;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private TabLayout mTabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        init(view);
        return view;
    }

    @Override
    protected void init(View view) {
        mParent = (HomeActivity) getActivity();
        mParent.getSupportActionBar().setTitle("Search for Meals");

        //init fragments
        mList = ListSearchFragment.newInstance();
        mMap = MapSearchFragment.newInstance();

        //init view pager
        viewPager = (ViewPager) view.findViewById(R.id.search_viewpager);
        setupViewPager(viewPager);
        adapter = ((ViewPagerAdapter) viewPager.getAdapter());

        //init tabLayout
        mTabLayout = (TabLayout) view.findViewById(R.id.search_tabs);
        mTabLayout.setupWithViewPager(viewPager);

        loadDefaultScreen();
    }

    private void setupViewPager(final ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPager.setOffscreenPageLimit(1);
        adapter.addFragment(mList, "List");
        adapter.addFragment(mMap, "Map");
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
            // lets see if the currentFragment or any of its childFragment can handle onBackPressed
            return currentFragment.onBackPressed();
        }

        // this Fragment couldn't handle the onBackPressed call
        return false;
    }
}
