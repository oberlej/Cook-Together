package com.cooktogether.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import com.cooktogether.helpers.AbstractBaseFragment;
import com.cooktogether.helpers.DownloadImage;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends AbstractBaseFragment implements View.OnClickListener {

    private static final int SELECT_PICTURE = 1234;
    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA = 1293;
    private CircleImageView mPicture;

    private User mUser = null;
    private boolean mAnswer;
    private EditText mUserName;
    private TextView mUseFBPicture;
    private ImageView mDeletePicture;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        init(view);
        loadUser();
        return view;
    }

    @Override
    protected void init(View view) {
        mParent = (HomeActivity) getActivity();
        mPicture = (CircleImageView) view.findViewById(R.id.profile_picture);
        mPicture.setOnClickListener(this);
        mUserName = (EditText) view.findViewById(R.id.profile_user_name);
        mUseFBPicture = (TextView) view.findViewById(R.id.profile_use_fb_picture);
        mUseFBPicture.setOnClickListener(this);
        mDeletePicture = (ImageView) view.findViewById(R.id.profile_delete_picture);
        mDeletePicture.setOnClickListener(this);
    }

    private void loadUser() {
        getDB().child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(getContext(), "Failed to load profile. Please try logging out and back in.", Toast.LENGTH_LONG).show();
                    ((HomeActivity) mParent).loadDefaultScreen();
                    return;
                }
                mUser = User.parseSnapshot(dataSnapshot);
                //user name
                if (mUser.getUserName() != null && !mUser.getUserName().isEmpty()) {
                    mUserName.setText(mUser.getUserName());
                }
                //profile pic
                loadPicture();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load profile.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private Bitmap readPicture(String name) {
        File f = new File(getContext().getDir("profile_pictures", Context.MODE_PRIVATE), name + ".jpg");
        Bitmap b = null;
        if (f != null) {
            b = BitmapFactory.decodeFile(f.getPath(), null);
        }
        return b;
    }

    private void loadPicture() {
        if (mUser.getPictureURI().isEmpty()) {
            if (mUser.isFacebookConnected()) {
                setFacebookPicture();
            } else {
                resetPicture();
            }
        } else {
            Bitmap picture = readPicture(getUid());
            if (picture == null) {
                writePicture(getUid());
            } else {
                mPicture.setImageBitmap(picture);
            }
            mDeletePicture.setVisibility(View.VISIBLE);
            if (mUser.isFacebookPicture()) {
                mUseFBPicture.setVisibility(View.GONE);
            } else {
                mUseFBPicture.setVisibility(View.VISIBLE);
            }
        }
    }

    private void uploadPicture() {
        // Get the data from an ImageView as bytes
        mPicture.setDrawingCacheEnabled(true);
        mPicture.buildDrawingCache();
        Bitmap bitmap = mPicture.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = getRootRef().child("profile_pictures").child(getUid()).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getContext(), "Failed to upload the profile picture. Please try again.", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });
    }

    private void writePicture(final String name) {
        StorageReference ref = getRootRef().child("profile_pictures").child(name);

        File tmp = new File(getContext().getDir("profile_pictures", Context.MODE_PRIVATE) + "/" + name + ".jpg");
        tmp.deleteOnExit();

        final File picture = new File(getContext().getDir("profile_pictures", Context.MODE_PRIVATE), name + ".jpg");


        ref.getFile(picture).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap b = BitmapFactory.decodeFile(picture.getPath(), null);
                if (b != null) {
                    mPicture.setImageBitmap(b);
                } else {
                    resetPicture();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getContext(), "Failed to load the picture " + name + ". Please try again.", Toast.LENGTH_LONG).show();
                resetPicture();
            }
        });
    }

    private void setFacebookPictureUri() {
        String facebookUserId = "";
        // find the Facebook profile and get the user's id
        for (UserInfo profile : getCurrentUser().getProviderData()) {
            // check if the provider id matches "facebook.com"
            if (profile.getProviderId().equals(getString(R.string.facebook_provider_id))) {
                facebookUserId = profile.getUid();
            }
        }
        mUser.setPictureURI("https://graph.facebook.com/" + facebookUserId + "/picture?type=large");
        mUser.setFacebookPicture(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //=> calls on create options menu
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
                                Toast.makeText(getContext(), "Changes discarded.", Toast.LENGTH_LONG).show();
                                ((HomeActivity) mParent).loadDefaultScreen();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Discard changes ?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

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
        if (requestCode == SELECT_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                mUser.setPictureURI(data.getData().toString());
                mUser.setFacebookPicture(false);
                Bitmap bitmap = getPath(data.getData());
                mPicture.setImageBitmap(bitmap);
                mUseFBPicture.setVisibility(View.VISIBLE);
                mDeletePicture.setVisibility(View.VISIBLE);
                uploadPicture();
                writePicture(getUid());
            } else {
                mUseFBPicture.setVisibility(View.VISIBLE);
                mDeletePicture.setVisibility(View.GONE);
                mUser.setFacebookPicture(false);
                mUser.setPictureURI("");
                mPicture.setBackgroundResource(R.drawable.ic_photo_camera_black_48dp);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_MEDIA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPicture.setImageBitmap(getPath(Uri.parse(mUser.getPictureURI())));
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
            Toast.makeText(getContext(), "Your picture could not be found. Please try a different one.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "There has been an unknown error. Please try again.", Toast.LENGTH_LONG).show();
        }
        return bitmap;
    }

    private void selectPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_picture:
                selectPicture();
                break;
            case R.id.profile_use_fb_picture:
                setFacebookPicture();
                break;
            case R.id.profile_delete_picture:
                if (!mUser.getPictureURI().isEmpty()) {
                    resetPicture();
                }
                break;
        }
    }

    private void setFacebookPicture() {
        setFacebookPictureUri();
        new DownloadImage().execute(mUser.getPictureURI());
    }

    private void resetPicture() {
        mUser.setFacebookPicture(false);
        mUser.setPictureURI("");
        mUseFBPicture.setVisibility(View.VISIBLE);
        mDeletePicture.setVisibility(View.GONE);
        mPicture.setImageBitmap(null);
        mPicture.setBackgroundResource(R.drawable.ic_photo_camera_black_48dp);
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        public DownloadImage() {
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(mUser.getPictureURI()).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                mPicture.setImageBitmap(result);
                mUseFBPicture.setVisibility(View.GONE);
                mDeletePicture.setVisibility(View.VISIBLE);
                uploadPicture();
                writePicture(getUid());
            } else {
                Toast.makeText(getContext(), "Failed to download your facebook profile picture. Please try again.", Toast.LENGTH_LONG).show();
                resetPicture();
            }
        }
    }
}
