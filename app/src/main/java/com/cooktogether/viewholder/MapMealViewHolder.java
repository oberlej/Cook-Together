package com.cooktogether.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.model.Meal;

/**
 * Created by hela on 08/12/16.
 */

public class MapMealViewHolder extends MealViewHolder {

    public TextView title;
    public TextView description;

    public MapMealViewHolder(View itemView) {
        super(itemView);

    }

    public void bindToPost(Meal meal) {
        super.bindToPost(meal);
    }
}