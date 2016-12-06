package com.cooktogether;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;
import java.util.jar.Manifest;

public class Map extends AppCompatActivity {
    LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        String location_context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getSystemService(location_context);
        testProviders();
    }

    public void testProviders() {

        TextView tv = (TextView) findViewById(R.id.myTextView);
        StringBuilder sb = new StringBuilder("Enabled Providers:");
        List<String> providers = locationManager.getProviders(true);

        for(String provider: providers) {
            locationManager.requestLocationUpdates(provider, 1000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
            sb.append("\n ").append(provider).append(": ");
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(provider);
                if(location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    sb.append(latitude).append(", ").append(longitude);
                } else {
                    sb.append("No location");
                }
            } else {
                sb.append("Permission denied!");
            }
        }
        tv.setText(sb);
    }

}
