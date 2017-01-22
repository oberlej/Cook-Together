package com.cooktogether.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.helpers.UploadPicture;
import com.cooktogether.model.Meal;
import com.cooktogether.model.User;
import com.cooktogether.viewholder.MealViewHolder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

/**
 * Created by hela on 15/01/17.
 */
//todo check if image loader params are needed
public class MealsListAdapter extends RecyclerView.Adapter<MealViewHolder> {
    private ArrayList<Meal> meals;
    private HashMap<String, User> users;
    //for the image loader
    private Context context;
    private StorageReference rootRef;
    private DatabaseReference db;

    public MealsListAdapter(ArrayList<Meal> meals) {
        this.meals = meals;
        this.users = new HashMap<String, User>();
    }

    public MealsListAdapter() {
        meals = new ArrayList<>();
        users = new HashMap<String, User>();
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
        TextView mealStatus = (TextView) holder.itemView.findViewById(R.id.meal_status);
        ProgressBar mealRsvProgress = (ProgressBar) holder.itemView.findViewById(R.id.reservations_progress_bar);
        TextView progressTxt = (TextView) holder.itemView.findViewById(R.id.progress_bar_txt);
        if (m.getBooked()) {
            mealStatus.setText("BOOKED");
            mealStatus.setTextColor(Color.RED); //color.rsv_red
            holder.itemView.findViewById(R.id.progress_bar_layout).setVisibility(View.GONE);
            mealStatus.setVisibility(View.VISIBLE);
        } else {
            mealRsvProgress.setMax(m.getNbrPersons());
            mealRsvProgress.setProgress(m.getNbrReservations());
            progressTxt.setText(m.getNbrReservations() + "/" + m.getNbrPersons() + " places reserved");
            progressTxt.setTextColor(Color.rgb(50, 205, 50)); //color.rsv_green;
            mealStatus.setVisibility(GONE);
            holder.itemView.findViewById(R.id.progress_bar_layout).setVisibility(View.VISIBLE);
        }

        //setting user pic
        CircleImageView pic = (CircleImageView) holder.itemView.findViewById(R.id.profile_pic);
        if (!users.isEmpty()) { //empty when the user is on my meals list fragment
            User user = users.get(m.getMealKey());

            new UploadPicture(context, user, pic, null, rootRef, db).loadPicture();
        } else
            pic.setVisibility(GONE);

        holder.bindToPost(m);
    }

    @Override
    public int getItemCount() {
        return this.meals.size();
    }

    public void setMeals(ArrayList<Meal> meals) {
        this.meals = meals;
    }

    public void setUsers(HashMap<String, User> users) {
        this.users = users;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setRootRef(StorageReference rootRef) {
        this.rootRef = rootRef;
    }

    public void setDb(DatabaseReference db) {
        this.db = db;
    }

    public void cleanup() {
        meals.clear();
        users.clear();
    }

    public Meal getSelectedMeal(int position) {
        return meals.get(position);
    }
}
