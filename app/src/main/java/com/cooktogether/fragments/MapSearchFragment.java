package com.cooktogether.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.helpers.AbstractBaseFragment;
import com.cooktogether.helpers.LocationBarFragment;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Meal;
import com.cooktogether.model.UserLocation;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by hela on 08/01/17.
 */

public class MapSearchFragment extends AbstractBaseFragment implements OnMapReadyCallback {
    private GoogleMap myGoogleMap;
    private ArrayList<Meal> nearByMealsList;
    private UserLocation mLocation;
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
            ((TextView) this.getView().findViewById(R.id.myLocationText)).setText("Permission denied");
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
                        //((HomeActivity) mParent).selectDrawerItem(((HomeActivity) mParent).getNvDrawer().getMenu().findItem(R.id.nav_meal_detail), getString(R.string.update_meal));
                    }
                }
            });

            LocationManager locationManager;
            String context = Context.LOCATION_SERVICE;

            locationManager = (LocationManager) mParent.getSystemService(context);

            /*Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);

            String provider = locationManager.getBestProvider(criteria, true);
            TextView myLocationText = (TextView) this.getView().findViewById(R.id.myLocationText);
            myLocationText.setText("Locating using " + provider);

            Location location ;//= new Location(provider);
            location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                updateWithNewLocation(location, getView());
            }
            */
            /*Location location = new Location("provider");
            if(mLocation != null) {
                location.setLatitude(mLocation.getLatitude());
                location.setLongitude(mLocation.getLongitude());
            }*/
            //locationManager.requestLocationUpdates("provider", 2000, 10, locationListener);

        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location, getView());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
            updateWithNewLocation(null, getView());
        }
    };

    private ArrayList<Meal> findNearByMeals(DataSnapshot meals) {
        ArrayList<Meal> nearByMeals = new ArrayList<Meal>();

        for (DataSnapshot mealSnap : meals.getChildren()) {
            Meal meal = Meal.parseSnapshot(mealSnap);
            nearByMeals.add(meal);
        }
        return nearByMeals;
    }


    private void updateWithNewLocation(android.location.Location location, View view) {
        String latitudeLongitude;
        TextView myLocationText = (TextView) view.findViewById(R.id.myLocationText);

        String addressString = "No address found";

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            //updating mLocation
            latitudeLongitude = "lat: " + latitude + " long: " + longitude;
            mLocation.setLatitude(latitude);
            mLocation.setLongitude(longitude);
            mLocation.setName(latitudeLongitude);

            CameraPosition cameraPos = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(10).build();

            myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

            //removes all the marker in the map
            myGoogleMap.clear();
            //add the markers
            addMarkerTo("mine", mLocation, "My position");
            if (!nearByMealsList.isEmpty()) {
                for (Meal m : nearByMealsList) {
                    addMarkerTo(m.getMealKey(), m.getLocation(), m.getTitle());
                }
            }

            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                StringBuilder stringBuilder = new StringBuilder();
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);

                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        stringBuilder.append("\n").append(address.getAddressLine(i));
                    }
                    addressString = stringBuilder.toString();
                    mLocation.setName(address.getLocality());
                }
            } catch (IOException e) {
                Log.e("update geocoder error:", e.getMessage());
            }
        } else {
            myGoogleMap.clear();
            //add the markers
            if (!nearByMealsList.isEmpty()) {
                for (Meal m : nearByMealsList) {
                    addMarkerTo(m.getMealKey(), m.getLocation(), m.getTitle());
                }
            }
            latitudeLongitude = "No location found";
            //mLocation.setName(latitudeLongitude);
        }

        //myLocationText.setText("Your position is:" + addressString);

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
        Query allPosts = databaseReference.child("meals");
        return allPosts;
    }

    protected void init(View view, Bundle savedInstanceState) {
        mParent = (HomeActivity) getActivity();

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) view.findViewById(R.id.myMap);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);
        //final Fragment locFrag = (LocalizationFragment)mParent.getSupportFragmentManager().findFragmentById(R.id.search_location);
        //final Fragment locFrag = mParent.getSupportFragmentManager().findFragmentById(R.id.search_location);
        final Fragment locFrag = LocationBarFragment.newInstance();
        mParent.getSupportFragmentManager().beginTransaction().replace(R.id.search_location, locFrag).commit();

        /*view.findViewById(R.id.search_location).findViewById(R.id.enter_location_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocation = ((LocalizationFragment) locFrag).getSelectedLocation();
            }
        });*/

        mLocation = ((LocationBarFragment) locFrag).getSelectedLocation();

        //getting the list of other meal propositions
        nearByMealsList = new ArrayList<Meal>();
        Query mealsQuery = getQuery(getDB());

        mealsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot meals) {
                nearByMealsList = findNearByMeals(meals);
                Location location;
                if(mLocation == null)
                    location = null;
                else {
                    location = new Location("provider");
                    location.setLatitude(mLocation.getLatitude());
                    location.setLongitude(mLocation.getLongitude());
                }
                updateWithNewLocation(location, getView());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });

        //((SupportMapFragment) mParent.getSupportFragmentManager().findFragmentById(R.id.myMap)).getMapAsync(this);

    }

    @Override
    protected void init(View view) {

    }

    /* public void goToLocation(View view){
        //get the input of the user
        EditText query = (EditText) view.findViewById(R.id.searchLocation);
        //convert it to string
        String locationName = query.getText().toString();

        String addressString = "No address";
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        Location location = new Location("");
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            StringBuilder stringBuilder = new StringBuilder();
            if(addresses.size() > 0) {
                Address address = addresses.get(0);

                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++){
                    stringBuilder.append("\n").append(address.getAddressLine(i));
                }
                stringBuilder.append(address.getCountryName());

                location.setLongitude(address.getLongitude());
                location.setLatitude(address.getLatitude());
                updateWithNewLocation(location, getView());

                addressString = stringBuilder.toString();

            }
        } catch (IOException e){
            Log.e("goToLocation: ", e.getMessage());
        }
        System.out.println(addressString);
    }
*/
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
}
