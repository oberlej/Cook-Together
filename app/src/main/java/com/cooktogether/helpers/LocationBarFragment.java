package com.cooktogether.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.adapter.locationOptionsAdapter;
import com.cooktogether.listener.RecyclerItemClickListener;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.User;
import com.cooktogether.model.UserLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationBarFragment extends AbstractBaseFragment implements View.OnClickListener{

    private EditText mLocationName;

    // for the list of location options
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private UserLocation selectedLocation;

    public LocationBarFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_location_bar, container, false);
        init(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(false);
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
    protected void init(final View view) {
        mParent = (HomeActivity) getActivity();
        initSelectedLocation();
        initLocationName(view);
        view.findViewById(R.id.enter_location_btn).setOnClickListener(this);
    }

    /*
    For now it returns only the first address found to make it simple
     */
    private ArrayList<UserLocation> getLocation(String locationName) {
        Geocoder geo = new Geocoder(getContext(), Locale.getDefault());

        ArrayList<UserLocation> locations = new ArrayList<UserLocation>();

        try {
            List<Address> addresses = geo.getFromLocationName(locationName, 5);
            if (addresses.size() > 0) {
                for (Address address : addresses) {
                    UserLocation location = new UserLocation();
                    location.setLatitude(address.getLatitude());
                    location.setLongitude(address.getLongitude());
                    location.setAddress(address);
                    locations.add(location);
                }
            }
        } catch (IOException e) {
            Log.e("getLocation", e.getMessage());
        }
        return locations;
    }

    public UserLocation getSelectedLocation() {
        return selectedLocation;
    }

    public void setSelectedLocation(UserLocation location) {
        this.selectedLocation = location;
    }

    public static LocationBarFragment newInstance() {
        return new LocationBarFragment();
    }

    public void initLocationName(View view) {
        //for the list of location options
        mRecyclerView = (RecyclerView) view.findViewById(R.id.location_options);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        selectedLocation = ((locationOptionsAdapter) mAdapter).getSelectedLocation(position);
                        mLocationName.setText(selectedLocation.toString());
                        ((locationOptionsAdapter) mAdapter).clear();
                        mRecyclerView.clearFocus();
                    }
                })
        );

        mLocationName = (EditText) view.findViewById(R.id.create_location);
        mLocationName.setText(selectedLocation.getName());

        mLocationName.addTextChangedListener(new TextWatcher() {
            ArrayList<UserLocation> locations = new ArrayList<UserLocation>();

            public void afterTextChanged(Editable s) {

                if (mLocationName.isFocused()) {
                    locations = getLocation(mLocationName.getText().toString());
                    if (locations.isEmpty()) {
                        mLocationName.setError("Location is not found, please try again");
                    } else {
                        mAdapter = new locationOptionsAdapter();
                        mRecyclerView.setAdapter(mAdapter);
                        ((locationOptionsAdapter) mAdapter).setMOptions(locations);
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

    }

    public void initSelectedLocation() {
        LocationManager locationManager;
        String context = Context.LOCATION_SERVICE;

        locationManager = (LocationManager) mParent.getSystemService(context);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        String provider = locationManager.getBestProvider(criteria, true);

        Location location;//= new Location(provider);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Cannot access to your current location, please verify your localization permission", Toast.LENGTH_LONG).show();
        }
        location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            setSelectedLocation(new UserLocation(lat, lon, getAddress(lat, lon)));
        }
        else{
            setSelectedLocation(new UserLocation(0,0));
        }
        locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            setSelectedLocation(new UserLocation(lat, lon, getAddress(lat, lon)));
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
    };

    private Address getAddress(double latitude, double longitude){
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            StringBuilder stringBuilder = new StringBuilder();
            if(addresses.size() > 0) {
                return addresses.get(0);
            }
        } catch (IOException e){
            Log.e("update geocoder error:", e.getMessage());
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.enter_location_btn :
                setSelectedLocation(getLocation(mLocationName.getText().toString()).get(0));
                break;
        }
    }

}
