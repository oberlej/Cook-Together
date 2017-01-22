package com.cooktogether.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Meal;
import com.cooktogether.viewholder.MealViewHolder;

import java.util.ArrayList;

import static android.view.View.GONE;

/**
 * Created by hela on 15/01/17.
 */

public class MealsListAdapter extends RecyclerView.Adapter<MealViewHolder>{
    private ArrayList<Meal> meals;

    public MealsListAdapter(ArrayList<Meal> meals) {
        this.meals = meals;
    }
    public MealsListAdapter(){
        meals = new ArrayList<>();
    }

    @Override
    public MealViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        MealViewHolder vMeal = new MealViewHolder(v);

        return vMeal;
    }

    @Override
    public void onBindViewHolder(MealViewHolder holder, int position) {
        Meal m = meals.get(position);
        TextView mealStatus = (TextView)holder.itemView.findViewById(R.id.meal_status);
        ProgressBar mealRsvProgress = (ProgressBar) holder.itemView.findViewById(R.id.reservations_progress_bar);
        TextView progressTxt = (TextView) holder.itemView.findViewById(R.id.progress_bar_txt);
        if(m.getBooked()) {
            mealStatus.setText("BOOKED");
            mealStatus.setTextColor(Color.RED);
            holder.itemView.findViewById(R.id.progress_bar_layout).setVisibility(View.GONE);
            mealStatus.setVisibility(View.VISIBLE);
        }
        else{
            mealRsvProgress.setMax(m.getNbrPersons());
            mealRsvProgress.setProgress(m.getNbrReservations());
            progressTxt.setText(m.getNbrReservations() +"/"+ m.getNbrPersons()+" places reserved");
            progressTxt.setTextColor(Color.GREEN);
            mealStatus.setVisibility(GONE);
            holder.itemView.findViewById(R.id.progress_bar_layout).setVisibility(View.VISIBLE);
        }

        holder.bindToPost(m);
    }

    @Override
    public int getItemCount() {
        return this.meals.size();
    }

    public void setMeals(ArrayList<Meal> meals) {
        this.meals = meals;
    }

    public void cleanup(){
        meals.clear();
    }

    public Meal getSelectedMeal(int position) {
        return meals.get(position);
    }
}
