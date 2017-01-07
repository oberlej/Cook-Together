package com.cooktogether.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.helpers.AbstractBaseFragment;
import com.cooktogether.helpers.DownloadImage;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.User;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends AbstractBaseFragment implements View.OnClickListener {

    private static final int SELECT_PICTURE = 1234;
    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA = 1293;
    private CircleImageView mImage;

    private User mUser = null;
    private boolean mAnswer;
    private EditText mUserName;
    private TextView mUseFBImage;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        init(view);
        return view;
    }

    @Override
    protected void init(View view) {
        mParent = (HomeActivity) getActivity();
        mImage = (CircleImageView) view.findViewById(R.id.profile_image);
        mImage.setOnClickListener(this);
        mUserName = (EditText) view.findViewById(R.id.profile_user_name);
        mUseFBImage = (TextView) view.findViewById(R.id.profile_use_fb_image);
        mUseFBImage.setOnClickListener(this);
        loadUser();
    }

    private void loadUser() {
        getDB().child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(getContext(), "Failed to load profile.", Toast.LENGTH_LONG).show();
                    return;
                }
                mUser = User.parseSnapshot(dataSnapshot);
                //user name
                if (mUser.getUserName() != null && !mUser.getUserName().isEmpty()) {
                    mUserName.setText(mUser.getUserName());
                }
                //profile pic
                if (mUser.getImageURI().isEmpty()) {
                    if (mUser.isFacebookConnected()) {
                        setFacebookImageUri();
                        new DownloadImage(mImage).execute(mUser.getImageURI());
                        mUseFBImage.setVisibility(View.GONE);
                    }
                } else {
                    if (mUser.isFacebookImage()) {
                        new DownloadImage(mImage).execute(mUser.getImageURI());
                        mUseFBImage.setVisibility(View.GONE);
                    } else {
                        Bitmap b = getPath(Uri.parse(mUser.getImageURI()));
                        if (b != null) {
                            mImage.setImageBitmap(b);
                        } else {
                            mImage.setBackgroundResource(R.drawable.ic_photo_camera_black_48dp);
                        }
                        mUseFBImage.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load profile.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setFacebookImageUri() {
        String facebookUserId = "";
        // find the Facebook profile and get the user's id
        for (UserInfo profile : getCurrentUser().getProviderData()) {
            // check if the provider id matches "facebook.com"
            if (profile.getProviderId().equals(getString(R.string.facebook_provider_id))) {
                facebookUserId = profile.getUid();
            }
        }
        mUser.setImageURI("https://graph.facebook.com/" + facebookUserId + "/picture?type=large");
        mUser.setFacebookImage(true);
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
                Toast.makeText(getContext(), "Profile updated.", Toast.LENGTH_LONG).show();
                ((HomeActivity) mParent).loadDefaultScreen();
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
                    ((HomeActivity) mParent).loadDefaultScreen();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProfile() {
        //update mUser
        mUser.setUserName(mUserName.getText().toString());
        //save mUser
        getDB().child("users").child(getUid()).setValue(mUser);
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
            mUser.setImageURI(data.getData().toString());
            mUser.setFacebookImage(false);
            Bitmap bitmap = getPath(data.getData());
            mImage.setImageBitmap(bitmap);
            mUseFBImage.setVisibility(View.VISIBLE);
        } else {
            mImage.setBackgroundResource(R.drawable.ic_photo_camera_black_48dp);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_MEDIA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mImage.setImageBitmap(getPath(Uri.parse(mUser.getImageURI())));
                }
                return;
            }
        }
    }

    private Bitmap getPath(Uri uri) {
        Bitmap bitmap = null;
        try {
            int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);

            } else {
                bitmap = MediaStore.Images.Media.getBitmap(mParent.getContentResolver(), uri);
            }
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
            case R.id.profile_use_fb_image:
                setFacebookImageUri();
                new DownloadImage(mImage).execute(mUser.getImageURI());
                mUseFBImage.setVisibility(View.GONE);
                break;
        }
    }
}
