package com.cooktogether.model;

import android.location.Address;
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

    public UserLocation(double latitude, double longitude, Address address){
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = "lat: " + latitude+ "long: " +longitude;
        this.address = address;
        if(address != null)
            this.name = address.getLocality();
    }

    public void setAddress(Address add){
        this.address = add;
        this.name = add.getLocality();
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
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

    public Address getAddress(){
        return this.address;
    }


}
