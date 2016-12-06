package com.cooktogether.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.model.Meal;

public class MealViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public TextView description;

    public MealViewHolder(View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.meal_title);
        description = (TextView) itemView.findViewById(R.id.meal_description);
    }

    public void bindToPost(Meal meal) {
        title.setText(meal.getTitle());
        description.setText(meal.getDescription());
    }
}
