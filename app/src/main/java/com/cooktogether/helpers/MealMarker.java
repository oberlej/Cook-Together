package com.cooktogether.helpers;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by hela on 16/01/17.
 */

public class MealMarker implements ClusterItem {
    private final LatLng mPosition;
    private String mtitle; //meal title
    private String mSnippet; //meal location in this case
    private String mkey; //user id ??

    public MealMarker(double lat, double lng, String title, String snippet, String key) {
        mPosition = new LatLng(lat, lng);
        mtitle = title;
        mSnippet = snippet;
        mkey = key;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getMtitle() {
        return mtitle;
    }

    public String getmSnippet() {
        return mSnippet;
    }

    public String getMkey() {
        return mkey;
    }
}