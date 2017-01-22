package com.cooktogether.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.helpers.AbstractLocationFragment;
import com.cooktogether.helpers.MealMarker;
import com.cooktogether.helpers.UploadPicture;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Meal;
import com.cooktogether.model.User;
import com.cooktogether.model.UserLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by hela on 08/01/17.
 */

public class MapSearchFragment extends AbstractLocationFragment implements OnMapReadyCallback {
    private GoogleMap myGoogleMap;
    private ArrayList<Meal> nearByMealsList;
    private HashMap<String, User> usersList; //key is mealKey , value is the user
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
        usersList = new HashMap<String, User>();

        Query mealsQuery = getQuery(getDB());

        mealsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot meals) {
                findNearByMeals(meals);
                //add the markers
                if(!nearByMealsList.isEmpty()){
                    for (Meal m : nearByMealsList) {
                        addMarkerTo(m.getMealKey(), m.getLocation(), m.getTitle());
                    }
                }
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


    private void setUpClusterer() {

        // Initialize the manager with the context and the map.
        mClusterManager = new ClusterManager<MealMarker>(getContext(), myGoogleMap);

        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MealMarker>() {
            @Override
            public void onClusterItemInfoWindowClick(MealMarker mealMarker) {
                ((HomeActivity) mParent).goToMeal((String) mealMarker.getmKey());
            }
        });

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.

        myGoogleMap.setOnCameraIdleListener(mClusterManager);
        myGoogleMap.setOnMarkerClickListener(mClusterManager);
        myGoogleMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        myGoogleMap.setOnInfoWindowClickListener(mClusterManager);
        myGoogleMap.setOnCameraIdleListener(mClusterManager);

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


    static final double COORDINATE_OFFSET = 1 / 80d; //can be adjusted

    //adds marker at the position latitude, longitude to the map , entitled title
    private void addMarkerTo(String mealKey, UserLocation location, String title) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //handle markers at the same position
        if (mMarkers.containsKey(latLng)) {
            int index = mMarkers.get(latLng);
            mMarkers.put(latLng, index + 1);
            latLng = new LatLng(location.getLatitude() + (index + 1) * COORDINATE_OFFSET, location.getLongitude() + (index + 1) * COORDINATE_OFFSET);
        } else {
            mMarkers.put(latLng, 0);
        }

        addItem(mealKey, latLng, location.toString(), title);
    }

    //adds item to the cluser
    private void addItem(String mealKey, LatLng location, String snippet, String title) {
        //creates the meal marker item
        MealMarker offsetItem = new MealMarker(location.latitude, location.longitude, title, snippet, mealKey);
        //add it to the cluster
        mClusterManager.addItem(offsetItem);
    }

    private void findNearByMeals(DataSnapshot mealsData) {
        nearByMealsList.clear();
        usersList.clear();

        for (DataSnapshot mealSnap : mealsData.getChildren()) {
            final Meal meal = Meal.parseSnapshot(mealSnap);
            if (!meal.getUserKey().equals(getUid())) {
                nearByMealsList.add(meal);
                usersList.put(meal.getMealKey(), null);
                getDB().child("users").child(meal.getUserKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = User.parseSnapshot(dataSnapshot);
                        usersList.put(meal.getMealKey(), user);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }


    private void updateWithNewLocation(UserLocation location) {

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();


            CameraPosition cameraPos = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(11).build();

            myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

        }

    }


    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child("meals");
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //removes all the marker in the map
        nearByMealsList.clear();
        usersList.clear();
        myGoogleMap.clear();
        mMarkers.clear();
        if (mClusterManager != null)
            mClusterManager.clearItems();
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

            tvTitle.setText(mSelectedItem.getmTitle());
            tvSnippet.setText(mSelectedItem.getmSnippet());

            CircleImageView userPic = (CircleImageView) myContentsView.findViewById(R.id.profile_pic);
            new UploadPicture(getContext(), usersList.get(mSelectedItem.getmKey()) , userPic, null, getRootRef(), getDB()).loadPicture();

            return myContentsView;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }
}
