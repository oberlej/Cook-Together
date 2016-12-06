package com.cooktogether;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SymbolTable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Localization extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap myGoogleMap;
    private Location mLocation;
    private ArrayList<Location> nearBy ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);

        ((MapFragment) getFragmentManager().findFragmentById(R.id.myMap)).getMapAsync(this);
        mLocation = null;
        nearBy = new ArrayList<Location>();
        /*
        LocationManager locationManager;
        String context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getSystemService(context);
        // using gps
        //String provider = LocationManager.GPS_PROVIDER;
        */
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ((TextView) findViewById(R.id.myLocationText)).setText("Permission denied");
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

            LocationManager locationManager;
            String context = Context.LOCATION_SERVICE;
            locationManager = (LocationManager) getSystemService(context);

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);

            String provider = locationManager.getBestProvider(criteria, true);
            TextView myLocationText = (TextView) findViewById(R.id.myLocationText);
            myLocationText.setText("Locating using " + provider);


            mLocation = locationManager.getLastKnownLocation(provider);
            if (mLocation != null) {
                updateWithNewLocation(mLocation);
            }
            locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);

        }
    }


    private void updateWithNewLocation(android.location.Location location) {
        String latitudeLongitude;
        TextView myLocationText = (TextView) findViewById(R.id.myLocationText);

        String addressString = "No address found";

        if(location != null){
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Location exp = mLocation;
            exp.setLatitude(mLocation.getLatitude()+5);
            exp.setLongitude(mLocation.getLongitude()+2);
            nearBy.add(exp);

            CameraPosition cameraPos = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(10).build();

            myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

            //removes all the marker in the map
            myGoogleMap.clear();
            //add the markers
            addMarkerTo(latitude, longitude, "My position");
            for(int i =0; i< nearBy.size(); i++) {
                addMarkerTo(nearBy.get(i).getLatitude(), nearBy.get(i).getLongitude(), "NEAR BY");
            }

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                StringBuilder stringBuilder = new StringBuilder();
                if(addresses.size() > 0) {
                    Address address = addresses.get(0);

                    for(int i = 0; i <= address.getMaxAddressLineIndex(); i++){
                        stringBuilder.append("\n").append(address.getAddressLine(i));
                    }
                    addressString = stringBuilder.toString();
                }
            } catch (IOException e){
                Log.e("update geocoder error:", e.getMessage());
            }
        } else {
            latitudeLongitude = "No location found";
        }

        myLocationText.setText("Your current position is:" + addressString);

    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
            updateWithNewLocation(null);
        }
    };

    public void goToLocation(View view){
        //get the input of the user
        EditText query = (EditText) findViewById(R.id.searchLocation);
        //convert it to string
        String locationName = query.getText().toString();

        String addressString = "No address";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            StringBuilder stringBuilder = new StringBuilder();
            System.out.print("size"+ addresses.size());
            if(addresses.size() > 0) {
                Address address = addresses.get(0);

                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++){
                    stringBuilder.append("\n").append(address.getAddressLine(i));
                }
                stringBuilder.append(address.getCountryName());
                mLocation.setLongitude(address.getLongitude());
                mLocation.setLatitude(address.getLatitude());
                updateWithNewLocation(mLocation);
                addressString = stringBuilder.toString();

            }
        } catch (IOException e){
            Log.e("goToLocation: ", e.getMessage());
        }
        System.out.println(addressString);
    }

    //adds marker at the position latitude, longitude to the map , entitled title
    private void addMarkerTo(double latitude, double longitude, String title){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(latitude, longitude));
        markerOptions.title(title);
        markerOptions.snippet("("+latitude+","+longitude+")");
        myGoogleMap.addMarker(markerOptions).showInfoWindow();
    }

    /*private Address getAddress(double latitude, double longitude){

    }

    private Address getAddress(String locationName){

    }*/
}
