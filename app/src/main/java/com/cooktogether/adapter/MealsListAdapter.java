package com.cooktogether.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooktogether.R;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Meal;
import com.cooktogether.viewholder.MealViewHolder;

import java.util.ArrayList;

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
        holder.bindToPost(meals.get(position));
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
