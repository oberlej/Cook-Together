package com.cooktogether.mainscreens;

import com.cooktogether.helpers.AbstractMealListActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyMealsActivity extends AbstractMealListActivity {

    public MyMealsActivity() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my posts
        return databaseReference.child("meals").orderByChild("userKey").equalTo(getUid());
    }
}
