package com.cooktogether.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.helpers.AbstractBaseFragment;
import com.cooktogether.helpers.UploadPicture;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends AbstractBaseFragment implements View.OnClickListener {

    private static final int SELECT_PICTURE = 1234;
    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA = 1293;
    private CircleImageView mPicture;
    private UploadPicture picLoader;

    private User mUser = null;
    private boolean mAnswer;
    private EditText mUserName;
    private EditText mDescription;
    private TextView mUseFBPicture;
    private ImageView mDeletePicture;

    static Calendar mCalendar;
    DatePickerFragment dateFragment;
    static final String DATE_FORMAT = "dd/MM/yyyy";
    static SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private static EditText mBirthDate;

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
        if(mUser!= null && !mUser.getUserKey().equals(getCurrentUser().getUid())) {
            disableEdit();
            mParent.getSupportActionBar().setTitle(mUser.getUserName() +" Profile");
            setHasOptionsMenu(false);
        }
        else {
            mParent.getSupportActionBar().setTitle("My Profile");
            setHasOptionsMenu(true);
        }
        ((HomeActivity)mParent).hideKeyboard(getContext());
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
        mBirthDate = (EditText) view.findViewById(R.id.profile_bday);
        mCalendar = Calendar.getInstance();
        mBirthDate.setOnClickListener(this);
        mDescription = (EditText) view.findViewById(R.id.profile_description);
    }

    private void loadUser() {
        mUser = ((HomeActivity)mParent).getToVisit()!= null ? ((HomeActivity)mParent).getToVisit() :((HomeActivity) mParent).getUser();
        if (mUser == null) {
            getDB().child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        Toast.makeText(getContext(), "Failed to load profile. Please try logging out and back in.", Toast.LENGTH_LONG).show();
                        ((HomeActivity) mParent).loadDefaultScreen();
                        return;
                    }
                    mUser = User.parseSnapshot(dataSnapshot);
                    if(((HomeActivity) mParent).getToVisit() == null) //loading the current user profile
                        ((HomeActivity) mParent).setUser(mUser);
                    mUserName.setText(mUser.getUserName());
                    mBirthDate.setText(mUser.getBirthDate());
                    mDescription.setText(mUser.getDescription());
                    //profile pic
                    picLoader = new UploadPicture(getContext(), mUser, mPicture, getCurrentUser(), getRootRef(), getDB());
                    loadPicture();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load profile.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            mUserName.setText(mUser.getUserName());
            mBirthDate.setText(mUser.getBirthDate());
            mDescription.setText(mUser.getDescription());
            //profile pic
            picLoader = new UploadPicture(getContext(), mUser, mPicture, getCurrentUser(), getRootRef(), getDB());
            loadPicture();
        }
    }

    /*private Bitmap readPicture() {
        File f = new File(getContext().getDir("profile_pictures", Context.MODE_PRIVATE), getUid() + ".jpg");
        Bitmap b = null;
        if (f != null) {
            b = BitmapFactory.decodeFile(f.getPath(), null);
        }
        return b;
    }*/

    private void loadPicture() {
        picLoader.loadPicture();

        if (mUser.isFacebookPicture()) {
            mUseFBPicture.setVisibility(View.GONE);
        } else {
            mUseFBPicture.setVisibility(View.VISIBLE);
        }

        if(picLoader.isPicSet())
            mDeletePicture.setVisibility(View.VISIBLE);
        else
            mDeletePicture.setVisibility(View.GONE);

        /* File picture = new File(getContext().getDir("profile_pictures", Context.MODE_PRIVATE) + "/" + getUid() + ".jpg");

        if (!picture.exists()) {
            if (mUser.isFacebookConnected() && mUser.isFacebookPicture()) {
                setFacebookPicture();
            } else {
                resetPicture();
            }
        } else {
            Bitmap b = BitmapFactory.decodeFile(picture.getPath(), null);
            if (b == null) {
                Toast.makeText(getContext(), "Failed to load your picture. Please try again.", Toast.LENGTH_LONG).show();
                resetPicture();
            } else {
                mPicture.setImageBitmap(b);
                mDeletePicture.setVisibility(View.VISIBLE);
                if (mUser.isFacebookPicture()) {
                    mUseFBPicture.setVisibility(View.GONE);
                } else {
                    mUseFBPicture.setVisibility(View.VISIBLE);
                }
            }
        }*/
    }

    /* private void uploadPicture() {
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

    private String getPath() {
        return getContext().getDir("profile_pictures", Context.MODE_PRIVATE) + "/" + getUid() + ".jpg";
    }

    private void writePicture() {
        //delete old
        File tmp = new File(getPath());
        if (tmp.exists()) {
            tmp.delete();
        }

        // Get the data from the ImageView as bytes
        mPicture.setDrawingCacheEnabled(true);
        mPicture.buildDrawingCache();
        Bitmap bitmap = mPicture.getDrawingCache();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        //create new
        File f = new File(getPath());
        try {
            if (f.createNewFile()) {
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                fo.close();
            } else {
                Toast.makeText(getContext(), "Failed to save your picture. Please try again.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to save your picture. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    private String getFacebookPictureUri() {
        String facebookUserId = "";
        // find the Facebook profile and get the user's id
        for (UserInfo profile : getCurrentUser().getProviderData()) {
            // check if the provider id matches "facebook.com"
            if (profile.getProviderId().equals(getString(R.string.facebook_provider_id))) {
                facebookUserId = profile.getUid();
            }
        }
        if (!facebookUserId.isEmpty()) {
            return "https://graph.facebook.com/" + facebookUserId + "/picture?type=large";
        }
        return "";
    }
    */

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //=> calls on create options menu
        setHasOptionsMenu(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                if(checkInputIsValid()) {
                    saveProfile();
                    Toast.makeText(getContext(), "Profile updated.", Toast.LENGTH_LONG).show();
                    ((HomeActivity) mParent).loadDefaultScreen();
                    return true;
                }
                else return false;
            case R.id.action_cancel:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        switch (choice) {
                            case DialogInterface.BUTTON_POSITIVE:
                                if(validUser()) {
                                    Toast.makeText(getContext(), "Changes discarded.", Toast.LENGTH_LONG).show();
                                    ((HomeActivity) mParent).loadDefaultScreen();
                                }
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

    private boolean validUser() {
        mUserName.setText(mUser.getUserName());
        mBirthDate.setText(mUser.getBirthDate());
        mDescription.setText(mUser.getDescription());

        return checkInputIsValid();
    }

    private boolean checkInputIsValid() {
        boolean valid = true;
        if (mUserName.getText() == null || mUserName.getText().toString().isEmpty()) {
            mUserName.setError("Please add your name before saving!");
            mUserName.requestFocus();
            valid = false;
        }
        if (mBirthDate.getText() == null || mBirthDate.getText().toString().isEmpty()) {
            mBirthDate.setError("Please specify your birthday before saving!");
            mBirthDate.requestFocus();
            valid = false;
        }
        return valid;
    }

    private void saveProfile() {
        //update mUser
        mUser.setUserName(mUserName.getText().toString());
        mUser.setBirthDate(mBirthDate.getText().toString());
        mUser.setDescription(mDescription.getText().toString());

        saveUser();

    }

    private void saveUser() {
        //save mUser
        //getDB().child("users").child(getUid()).setValue(mUser);
        DatabaseReference ref = getDB().child("users").child(getUid());
        ref.child("userName").setValue(mUser.getUserName());
        ref.child("birthDate").setValue(mUser.getBirthDate());
        ref.child("description").setValue(mUser.getDescription());
        ref.child("facebookPicture").setValue(mUser.isFacebookPicture());
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
                mUser.setFacebookPicture(false);
                Bitmap bitmap = getBitmap(data.getData());
                mPicture.setImageBitmap(bitmap);
                if (mUser.isFacebookConnected()) {
                    mUseFBPicture.setVisibility(View.VISIBLE);
                } else {
                    mUseFBPicture.setVisibility(View.GONE);
                }
                mDeletePicture.setVisibility(View.VISIBLE);
                picLoader.uploadPicture();
                picLoader.writePicture();
                saveUser();
                ((HomeActivity)mParent).loadPicture();
            } else {
                resetPicture();
                ((HomeActivity)mParent).resetPicture();
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
                    mPicture.setImageBitmap(getBitmap(Uri.parse(picLoader.getPath())));
                }
                return;
            }
        }
    }

    private Bitmap getBitmap(Uri uri) {
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
                resetPicture();
                break;
            case R.id.profile_bday:
                showDatePickerDialog();
                break;
        }
    }

    private void setFacebookPicture() {
        picLoader.setFacebookPicture();
        if (mUser.isFacebookPicture()) {
            mUseFBPicture.setVisibility(View.GONE);
            mDeletePicture.setVisibility(View.VISIBLE);
        }
        /*
        String uri = getFacebookPictureUri();
        if (!uri.isEmpty()) {
            new DownloadImage().execute(uri);
        } else {
            resetPicture();
        }
         */
    }

    private void resetPicture() {
        picLoader.resetPicture();
        if (mUser.isFacebookConnected()) {
            mUseFBPicture.setVisibility(View.VISIBLE);
        } else {
            mUseFBPicture.setVisibility(View.GONE);
        }
        mDeletePicture.setVisibility(View.GONE);
    }

  /*  private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        public DownloadImage() {
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urls[0]).openStream();
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
                mUser.setFacebookPicture(true);
                saveUser();
                uploadPicture();
                writePicture();
            } else {
                Toast.makeText(getContext(), "Failed to download your facebook profile picture. Please try again.", Toast.LENGTH_LONG).show();
                resetPicture();
            }
        }
    }
*/
    public void updateDateButtonText() {
        String dateForButton = dateFormat.format(mCalendar.getTime());
        mBirthDate.setText(dateForButton);
    }

    private void showDatePickerDialog() {
        dateFragment = new DatePickerFragment();
        dateFragment.show(getFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, day);
            String dateForButton = dateFormat.format(mCalendar.getTime());
            mBirthDate.setText(dateForButton);
        }
    }
    private void disableEdit(){
        mPicture.setClickable(false);
        mUserName.setEnabled(false);
        mUseFBPicture.setVisibility(View.GONE);
        mDeletePicture.setVisibility(View.GONE);
        mBirthDate.setEnabled(false);
        mDescription.setEnabled(false);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        ((HomeActivity)mParent).setToVisit(null);
    }
}
