package com.cooktogether.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.model.Meal;
import com.cooktogether.model.UserLocation;

public class MealViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public TextView description;
    public TextView location;

    public MealViewHolder(View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.meal_title);
        description = (TextView) itemView.findViewById(R.id.meal_description);
        location = (TextView) itemView.findViewById(R.id.meal_location);
    }

    public void bindToPost(Meal meal) {
        title.setText(meal.getTitle());
        description.setText(meal.getDescription());
        location.setText(meal.getLocation().toString());
    }
}
