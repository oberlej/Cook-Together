package com.cooktogether.helpers;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by hela on 16/01/17.
 */

public class MealMarker implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle; //meal title
    private String mSnippet; //meal location in this case
    private String mKey; //meal key

    public MealMarker(double lat, double lng, String title, String snippet, String key) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
        mKey = key;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmSnippet() {
        return mSnippet;
    }

    public String getmKey() {
        return mKey;
    }

}