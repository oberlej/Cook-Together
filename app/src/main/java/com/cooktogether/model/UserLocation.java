package com.cooktogether.model;

import android.location.Address;
import android.location.Location;
import android.provider.Telephony;

/**
 * Created by hela on 07/12/16.
 */

public class UserLocation {
    private double latitude;
    private double longitude;
    private Address address;
    private String name;

    public UserLocation(){
    }

    public UserLocation(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = "lat: " + latitude+ "long: " +longitude;
    }

    public UserLocation(UserLocation location){
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        //this.address = location.getAddress();
        this.name = location.getName();
    }
    public UserLocation(double latitude, double longitude, Address address){
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = "lat: " + latitude+ "long: " +longitude;
        this.address = address;
        if(address != null)
            this.name = addressToName(address);
    }

    public void setAddress(Address add){
        this.address = add;
        this.name = addressToName(add);
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
        this.name = "lat:" +latitude +"long: "+ this.longitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
        this.name = "lat:" +latitude +"long: "+ this.longitude;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    @Override
    public String toString(){
        return this.name;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    /*public Address getAddress(){
        return this.address;
    }*/
    /* transform the address into a name of location as it will appear to the user */

    private String addressToName(Address address){
        String locationName = "";
        if(address.getFeatureName() != null){
            locationName = address.getFeatureName();
        }
        if(address.getAdminArea() != null){
            if(address.getFeatureName() != address.getAdminArea())
                locationName += ", "+ address.getAdminArea();
        }
        locationName += ", " +address.getCountryName();
        return  locationName;
    }

}
