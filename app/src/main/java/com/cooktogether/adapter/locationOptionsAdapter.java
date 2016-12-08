package com.cooktogether.adapter;

import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.model.UserLocation;

import java.util.ArrayList;

/**
 * Created by hela on 08/12/16.
 */

public class locationOptionsAdapter extends RecyclerView.Adapter<locationOptionsAdapter.ViewHolder>{
    private ArrayList<UserLocation> moptions;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public locationOptionsAdapter() {
        moptions = new ArrayList<UserLocation>();
    }

    public void setMOptions(ArrayList<UserLocation> locations){
        moptions = (ArrayList<UserLocation>) locations.clone();
    }
    // Create new views (invoked by the layout manager)
    @Override
    public locationOptionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_option, parent, false);

        ViewHolder vh = new ViewHolder((TextView)v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(moptions.get(position).toString());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return moptions.size();
    }

    public UserLocation getSelectedLocation(int position){
        if(position >=0 && position < moptions.size()) {
            return moptions.get(position);
        }
        return null; //should not happen
    }

    public void clear(){
        moptions.clear();
    }
}
