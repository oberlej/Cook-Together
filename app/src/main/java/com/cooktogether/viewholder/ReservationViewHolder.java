package com.cooktogether.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.model.Meal;
import com.cooktogether.model.Reservation;

/**
 * Created by hela on 18/01/17.
 */

public class ReservationViewHolder  extends RecyclerView.ViewHolder{
    public TextView title;
    public TextView description;
    public TextView location;
    public TextView status;

    public ReservationViewHolder(View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.rsv_meal_title);
        description = (TextView) itemView.findViewById(R.id.rsv_meal_description);
        location = (TextView) itemView.findViewById(R.id.rsv_meal_location);
        status = (TextView) itemView.findViewById(R.id.rsv_status);
    }

    public void bindToPost(Reservation reservation, Meal m) {
        title.setText(m.getTitle());
        description.setText(m.getDescription());
        location.setText(m.getLocation().toString());
        status.setText(reservation.getStatus());
    }
}