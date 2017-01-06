package com.cooktogether.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.helpers.AbstractMealListFragment;
import com.cooktogether.helpers.DownloadImage;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final int SELECT_PICTURE = 1234;
    private HomeActivity mParent;
    private CircleImageView mImage;

    private FirebaseUser mUser;
    private boolean mAnswer;
    private EditText mUserName;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initFields(view);
        return view;
    }

    private void initFields(View view) {
        mParent = (HomeActivity) getActivity();
        mUser = mParent.getCurrentUser();
        mImage = (CircleImageView) view.findViewById(R.id.profile_image);
        mImage.setOnClickListener(this);
        mUserName = (EditText) view.findViewById(R.id.profile_user_name);

        if (mUser.getDisplayName() != null && !mUser.getDisplayName().isEmpty())
            mUserName.setText(mParent.getCurrentUser().getDisplayName());

        String facebookUserId = "";
        // find the Facebook profile and get the user's id
        for (UserInfo profile : mUser.getProviderData()) {
            // check if the provider id matches "facebook.com"
            if (profile.getProviderId().equals(getString(R.string.facebook_provider_id))) {
                facebookUserId = profile.getUid();
            }
        }
        String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?type=large";
        mImage.setTag(photoUrl);
        new DownloadImage(mImage).execute(photoUrl);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProfile();
                return true;
            case R.id.action_cancel:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        switch (choice) {
                            case DialogInterface.BUTTON_POSITIVE:
                                mAnswer = true;
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                mAnswer = false;
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Discard changes ?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                if (mAnswer) {
                    Toast.makeText(getContext(), "Changes discarded.", Toast.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProfile() {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(mUserName.getText().toString())
                .setPhotoUri(Uri.parse(mImage.getTag().toString()))
                .build();

        mUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Profile updated.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = getPath(data.getData());
            mImage.setImageBitmap(bitmap);
        }
    }

    private Bitmap getPath(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(mParent.getContentResolver(), uri);
        } catch (FileNotFoundException e) {
            Toast.makeText(getContext(), "Your image could not be found. Please try a different one.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "There has been an unknown error. Please try again.", Toast.LENGTH_LONG).show();
        }
        return bitmap;
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_image:
                selectImage();
                break;
        }
    }
}
