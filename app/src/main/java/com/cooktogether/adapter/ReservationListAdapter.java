package com.cooktogether.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooktogether.R;
import com.cooktogether.model.Meal;
import com.cooktogether.model.Reservation;
import com.cooktogether.viewholder.ReservationViewHolder;

import java.util.ArrayList;

/**
 * Created by hela on 18/01/17.
 */

public class ReservationListAdapter  extends RecyclerView.Adapter<ReservationViewHolder>{
    private ArrayList<Meal> meals;
    private ArrayList<Reservation> reservations;

    public ReservationListAdapter(ArrayList<Meal> meals, ArrayList<Reservation> reservations) {
        this.meals = meals;
        this.reservations = reservations;
    }
    public ReservationListAdapter(){
        meals = new ArrayList<>();
        reservations= new ArrayList<>();
    }

    @Override
    public ReservationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);

        ReservationViewHolder vRsv = new ReservationViewHolder(v);

        return vRsv;
    }

    @Override
    public void onBindViewHolder(ReservationViewHolder holder, int position) {
        holder.bindToPost(reservations.get(position), meals.get(position));
    }

    @Override
    public int getItemCount() {
        return this.meals.size();
    }

    public void setMeals(ArrayList<Meal> meals) {
        this.meals = meals;
    }

    public void setReservations(ArrayList<Reservation> reservations){
        this.reservations = reservations;
    }
    public void cleanup(){
        meals.clear();
        reservations.clear();
    }

    public Meal getSelectedMeal(int position) {
        return meals.get(position);
    }
}
