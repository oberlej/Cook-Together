package com.cooktogether.model;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeremiaoberle on 12/5/16.
 */

public class Meal {
    private String title;
    private String description;
    private String userKey;
    private String mealKey;
    private List<Day> freeDays;
    private UserLocation location;

    private int nbrPersons;
    private int nbrReservations;
    private Boolean booked;
    private List<String> reservations;

    public Meal() {

    }

    public Meal(String mealKey) {
        this.mealKey = mealKey;
    }

    /*    public Meal(String title, String description, String userKey, String mealKey, List<Day> freeDays,
                    UserLocation location, int nbr_persons) {
            this.title = title;
            this.description = description;
            this.userKey = userKey;
            this.mealKey = mealKey;
            this.freeDays = freeDays;
            this.location = location;
            this.nbr_persons = nbr_persons;
            this.nbr_reservations = 0;
            this.booked = false;
        }
    */
    public Meal(String title, String description, String userKey, String mealKey, List<Day> freeDays,
                UserLocation location, int nbr_persons, int nbr_reservations, Boolean booked) {
        this.title = title;
        this.description = description;
        this.userKey = userKey;
        this.mealKey = mealKey;
        this.freeDays = freeDays;
        this.location = location;
        this.nbrPersons = nbr_persons;
        this.nbrReservations = nbr_reservations;
        this.booked = booked;
    }

    public Meal(String title, String description, String userKey, String mealKey, List<Day> freeDays,
                UserLocation location, int nbr_persons, int nbr_reservations, Boolean booked,
                List<String> reservations) {
        this.title = title;
        this.description = description;
        this.userKey = userKey;
        this.mealKey = mealKey;
        this.freeDays = freeDays;
        this.location = location;
        this.nbrPersons = nbr_persons;
        this.nbrReservations = nbr_reservations;
        this.booked = booked;
        this.reservations = reservations;
    }

    public List<Day> getFreeDays() {
        return freeDays;
    }

    public void setFreeDays(List<Day> freeDays) {
        this.freeDays = freeDays;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getMealKey() {
        return mealKey;
    }

    public void setMealKey(String mealKey) {
        this.mealKey = mealKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserLocation getLocation() {
        return this.location;
    }

    public void setLocation(UserLocation location) {
        if (location != null) {
            this.location = location;
        }
    }

    public int getNbrPersons() {
        return nbrPersons;
    }

    public void setNbrPersons(int nbrPersons) {
        this.nbrPersons = nbrPersons;
    }

    public int getNbrReservations() {
        return nbrReservations;
    }

    public void setNbr_reservations(int nbrReservations) {
        this.nbrReservations = nbrReservations;

        if(this.nbrReservations == this.nbrPersons)
            setBooked(true);
    }

    public List<String> getReservations() {
        return reservations;
    }

    public void setReservations(List<String> reservations) {
        this.reservations = reservations;
    }

    public Boolean getBooked() {
        return booked;
    }

    public void setBooked(Boolean booked) {
        this.booked = booked;
    }

    public static Meal parseSnapshot(DataSnapshot snapshot) {
        List<Day> freeDays = new ArrayList<Day>();
        for(DataSnapshot day : snapshot.child("freeDays").getChildren()){
            freeDays.add(new Day((String)day.child("name").getValue(),(Boolean)day.child("lunch").getValue(),(Boolean)day.child("dinner").getValue()));
        }

        double lat = 0.0;
        double lon = 0.0;
        String locationName = "";

        if(snapshot.hasChild("location")) {
            DataSnapshot loc = snapshot.child("location");
            lat = getDouble(loc.child("latitude").getValue());
            lon = getDouble(loc.child("longitude").getValue());
            locationName = (String) loc.child("name").getValue();
        }

        UserLocation location = new UserLocation();
        location.setLongitude(lon);
        location.setLatitude(lat);
        location.setName(locationName);

        //reservation Part
        int nbr_persons = ((Long) snapshot.child("nbrPersons").getValue()).intValue();
        int nbr_reservations = ((Long) snapshot.child("nbrReservations").getValue()).intValue();
        boolean booked = (Boolean) snapshot.child("booked").getValue();

        List<String> reservations = new ArrayList<String>();
        for(DataSnapshot reservation : snapshot.child("reservations").getChildren()){
            reservations.add(reservation.getKey());
        }


        return new Meal((String) snapshot.child("title").getValue(), (String) snapshot.child("description").getValue(),
                (String) snapshot.child("userKey").getValue(), (String) snapshot.child("mealKey").getValue(),
                freeDays, location, nbr_persons, nbr_reservations, booked, reservations );
    }

    public static Double getDouble(Object firebaseValue){
        if(firebaseValue instanceof Long){
            return ((Long)firebaseValue).doubleValue();
        }
        return (Double) firebaseValue;
    }

    @Override
    public boolean equals(Object o){
        Meal m = (Meal)o;
        return (m!= null && m.getMealKey().equals(this.mealKey));
    }
}
