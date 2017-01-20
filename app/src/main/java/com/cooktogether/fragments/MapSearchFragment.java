package com.cooktogether.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.helpers.AbstractLocationFragment;
import com.cooktogether.helpers.MealMarker;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Meal;
import com.cooktogether.model.UserLocation;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hela on 08/01/17.
 */

public class MapSearchFragment extends AbstractLocationFragment implements OnMapReadyCallback {
    private GoogleMap myGoogleMap;
    private ArrayList<Meal> nearByMealsList;
    private MapView mapView;
    private HashMap<LatLng, Integer> mMarkers; //the key is the position and the value is the nbr of markers at the same position

    // Declare a variable for the cluster manager.
    private ClusterManager<MealMarker> mClusterManager;
    private MealMarker mSelectedItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_search, container, false);
        init(view, savedInstanceState);
        return view;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        myGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ((TextView) this.getView().findViewById(R.id.myLocationText)).setText("Permission denied, make sure to allow cook together to access to your location");
        } else {
            //Changing map type
            myGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            //showing/hiding your current location
            myGoogleMap.setMyLocationEnabled(true);
            //Enable/disable zooming controls
            myGoogleMap.getUiSettings().setZoomControlsEnabled(false);
            //Enable/disable my location button
            myGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            // Enable/disable compass icon
            myGoogleMap.getUiSettings().setCompassEnabled(true);
            // Enable/disable zooming functionality
            myGoogleMap.getUiSettings().setZoomGesturesEnabled(true);


            //initialize camera position on epfl if no other location provide
            CameraPosition cameraPos;
            double currentLat = 46.5198;
            double currentLong = 6.5657;
            if (getSelectedLocation() != null) {
                updateWithNewLocation(getSelectedLocation());
            } else {
                cameraPos = new CameraPosition.Builder().target(new LatLng(currentLat, currentLong)).zoom(10).build();
                myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
            }
        }
        setUpClusterer();
    }

    private ArrayList<Meal> findNearByMeals(DataSnapshot meals) {
        ArrayList<Meal> nearByMeals = new ArrayList<Meal>();

        for (DataSnapshot mealSnap : meals.getChildren()) {
            Meal meal = Meal.parseSnapshot(mealSnap);
            if (!meal.getUserKey().equals(getUid()))
                nearByMeals.add(meal);
        }
        return nearByMeals;
    }


    private void updateWithNewLocation(UserLocation location) {

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();


            CameraPosition cameraPos = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(10).build();

            myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

            //removes all the marker in the map
            myGoogleMap.clear();
            mMarkers.clear();
            if(mClusterManager != null)
                mClusterManager.clearItems();

            //add the markers
            if (!nearByMealsList.isEmpty()) {
                for (Meal m : nearByMealsList) {
                    addMarkerTo(m.getMealKey(), m.getLocation(), m.getTitle());
                }
            }

        } else {
            myGoogleMap.clear();
            //add the markers
            if (!nearByMealsList.isEmpty()) {
                for (Meal m : nearByMealsList) {
                    addMarkerTo(m.getMealKey(), m.getLocation(), m.getTitle());
                }
            }

        }

    }

    static final double COORDINATE_OFFSET = 1 / 60d; //can be adjusted

    //adds marker at the position latitude, longitude to the map , entitled title
    private void addMarkerTo(String id, UserLocation location, String title) {
        //// TODO: 15/01/17 handle markers at the same position
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //handle markers at the same position
        if (mMarkers.containsKey(latLng)) {
            int index = mMarkers.get(latLng);
            mMarkers.put(latLng, index + 1);
            latLng = new LatLng(location.getLatitude() + (index + 1) * COORDINATE_OFFSET, location.getLongitude() + (index + 1) * COORDINATE_OFFSET);
        } else {
            mMarkers.put(latLng, 0);
        }

        addItem(id, latLng, location.toString(), title);
    }

    //adds item to the cluser
    private void addItem(String id, LatLng location, String snippet, String title) {
        //creates the meal marker item
        MealMarker offsetItem = new MealMarker(location.latitude, location.longitude, title, snippet, id);
        //add it to the cluster
        mClusterManager.addItem(offsetItem);
    }

    private void setUpClusterer() {

        // Initialize the manager with the context and the map.
        mClusterManager = new ClusterManager<MealMarker>(getContext(), myGoogleMap);

        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MealMarker>() {
            @Override
            public void onClusterItemInfoWindowClick(MealMarker mealMarker) {
                ((HomeActivity) mParent).goToMeal((String) mealMarker.getMkey());
            }
        });

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.

        myGoogleMap.setOnCameraIdleListener(mClusterManager);
        myGoogleMap.setOnMarkerClickListener(mClusterManager);
        myGoogleMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        myGoogleMap.setOnInfoWindowClickListener(mClusterManager);

        mClusterManager
                .setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MealMarker>() {
                    @Override
                    public boolean onClusterItemClick(MealMarker item) {
                        mSelectedItem = item;
                        return false;
                    }
                });


        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(
                new MyCustomAdapterForItems());
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MyCustomAdapterForItems());

    }


    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child("meals");
    }

    protected void init(View view, Bundle savedInstanceState) {
        mParent = (HomeActivity) getActivity();

        //init location bar
        initLocationBar(view);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) view.findViewById(R.id.myMap);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);

        //getting the list of other meal propositions
        initMealsList();

        //init markers
        mMarkers = new HashMap<>();
    }

    private void initMealsList() {
        nearByMealsList = new ArrayList<Meal>();
        Query mealsQuery = getQuery(getDB());

        mealsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot meals) {
                nearByMealsList = findNearByMeals(meals);
                updateWithNewLocation(getSelectedLocation());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });

    }

    @Override
    protected void init(View view) {

    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    static public Fragment newInstance() {
        return new MapSearchFragment();
    }

    @Override
    public void setmButton(View v) {
        mEnterButton = (Button) v.findViewById(R.id.enter_location_btn);
        mEnterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWithNewLocation(getSelectedLocation());
            }
        });
    }

    @Override
    public void setmLocationName(View v) {
        mLocationName = (EditText) v.findViewById(R.id.create_location);
    }

    private class MyCustomAdapterForItems implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyCustomAdapterForItems() {
            myContentsView = mParent.getLayoutInflater().inflate(
                    R.layout.info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {

            TextView tvTitle = ((TextView) myContentsView
                    .findViewById(R.id.infoTitle));
            TextView tvSnippet = ((TextView) myContentsView
                    .findViewById(R.id.infoSnippet));

            tvTitle.setText(mSelectedItem.getMtitle());
            tvSnippet.setText(mSelectedItem.getmSnippet());
            return myContentsView;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }
}
