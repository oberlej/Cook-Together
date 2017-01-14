package com.cooktogether.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
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

import java.util.ArrayList;

/**
 * Created by hela on 08/01/17.
 */

public class MapSearchFragment extends AbstractLocationFragment implements OnMapReadyCallback {
    private GoogleMap myGoogleMap;
    private ArrayList<Meal> nearByMealsList;
    private MapView mapView;

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

            myGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker marker) {
                    if (marker.getTag() != "mine") {
                        ((HomeActivity) mParent).goToMeal((String) marker.getTag());
                    }
                }
            });


        }
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

            //add the markers
            if (!nearByMealsList.isEmpty()) {
                for (Meal m : nearByMealsList) {
                    addMarkerTo(m.getMealKey(), m.getLocation(), m.getTitle());
                }
            }
            addMarkerTo("mine", getSelectedLocation(), "My position");

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

    //adds marker at the position latitude, longitude to the map , entitled title
    private void addMarkerTo(String id, UserLocation location, String title) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
        markerOptions.title(title);
        markerOptions.snippet(location.toString());
        Marker myMarker = myGoogleMap.addMarker(markerOptions);
        myMarker.showInfoWindow();
        myMarker.setTag(id);
    }

    /*private Address getAddress(double latitude, double longitude){

    }

    private Address getAddress(String locationName){

    }*/

    public Query getQuery(DatabaseReference databaseReference) {
        Query othersPosts = databaseReference.child("meals").orderByChild("userKey").equalTo(false,getUid());
        return othersPosts;
    }

    protected void init(View view, Bundle savedInstanceState) {
        mParent = (HomeActivity) getActivity();

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) view.findViewById(R.id.myMap);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);

        //init location bar fragment
        setmButton(view);
        setmLocationName(view);
        initSelectedLocation();
        initLocationName(view);

        //getting the list of other meal propositions
        initMealsList();
    }

    private void initMealsList() {
        nearByMealsList = new ArrayList<Meal>();
        Query mealsQuery = getQuery(getDB());

        mealsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot meals) {
                nearByMealsList = findNearByMeals(meals);
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


    public Fragment newInstance() {
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
}
