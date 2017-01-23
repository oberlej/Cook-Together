package com.cooktogether.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.model.User;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by hela on 21/01/17.
 */

public class UploadPicture {

    private Context context;
    private User mUser;
    private ImageView mPicture;
    private FirebaseUser currentUser;
    private StorageReference rootRef;
    private DatabaseReference db;
    private boolean picSet = true;

    //Todo remove the currentUser: it should not be needed!
    public UploadPicture(Context context, User mUser, ImageView mPicture, FirebaseUser currentUser,
                         StorageReference rootRef, DatabaseReference db) {
        this.context = context;
        this.mUser = mUser;
        this.mPicture = mPicture;
        this.currentUser = currentUser;
        this.rootRef = rootRef;
        this.db = db;
    }

    public boolean isPicSet() {
        return picSet;
    }

    private void downloadPicture(final File f) {
        rootRef.child("profile_pictures").child(mUser.getUserKey()).getFile(f).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                setImage(f);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }

    private void setImage(File f) {
        Bitmap b = BitmapFactory.decodeFile(f.getPath(), null);
        if (b == null) {
            resetPicture();
        } else {
            mPicture.setImageBitmap(b);
        }
    }


    public void loadPicture() {
        File picture = new File(context.getDir("profile_pictures", Context.MODE_PRIVATE) + "/" + mUser.getUserKey() + ".jpg");
        if (!picture.exists()) {
            downloadPicture(picture);
        }else{
            setImage(picture);
        }
//        if (!picture.exists()) {
//            if (mUser.isFacebookConnected() && mUser.isFacebookPicture()) {
//                setFacebookPicture();
//            } else {
//                resetPicture();
//            }
//        } else {
//            Bitmap b = BitmapFactory.decodeFile(picture.getPath(), null);
//            if (b == null) {
//                Toast.makeText(context, "Failed to load your picture. Please try again.", Toast.LENGTH_LONG).show();
//                resetPicture();
//            } else {
//                mPicture.setImageBitmap(b);
//            }
//        }
    }

    public void setFacebookPicture() {
        String uri = getFacebookPictureUri();
        if (!uri.isEmpty()) {
            new DownloadImage().execute(uri);
        } else {
            resetPicture();
        }
    }

    public void resetPicture() {
        File tmp = new File(context.getDir("profile_pictures", Context.MODE_PRIVATE) + "/" + mUser.getUserKey() + ".jpg");
        if (tmp.exists()) {
            tmp.delete();
        }
        mUser.setFacebookPicture(false);
        mPicture.setImageBitmap(null);
        mPicture.setBackgroundResource(R.drawable.ic_photo_camera_black_48dp);
        picSet = false;
        db.child("users").child(mUser.getUserKey()).child("facebookPicture").setValue(false);
    }

    private String getFacebookPictureUri() {
        /*String facebookUserId = "";
        // find the Facebook profile and get the user's id
        for (UserInfo profile : currentUser.getProviderData()) {
            // check if the provider id matches "facebook.com"
            if (profile.getProviderId().equals(context.getString(R.string.facebook_provider_id))) {
                facebookUserId = profile.getUid();
            }
        }*/
        //if (!facebookUserId.isEmpty()) {
        return "https://graph.facebook.com/" + mUser.getUserKey() + "/picture?type=large";
        //}
        //return "";
    }

    public String getPath() {
        return context.getDir("profile_pictures", Context.MODE_PRIVATE) + "/" + mUser.getUserKey() + ".jpg";
    }

    public void uploadPicture() {
        // Get the data from an ImageView as bytes
        mPicture.setDrawingCacheEnabled(true);
        mPicture.buildDrawingCache();
        Bitmap bitmap = mPicture.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = rootRef.child("profile_pictures").child(mUser.getUserKey()).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Failed to upload the profile picture. Please try again.", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });
    }

    public void writePicture() {
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
                Toast.makeText(context, "Failed to save your picture. Please try again.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(context, "Failed to save your picture. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveUser() {
        //save mUser
        db.child("users").child(mUser.getUserKey()).setValue(mUser);
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

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
                mUser.setFacebookPicture(true);
                db.child("users").child(mUser.getUserKey()).child("facebookPicture").setValue(true);
                //saveUser();
                uploadPicture();
                writePicture();
            } else {
                Toast.makeText(context, "Failed to download your facebook profile picture. Please try again.", Toast.LENGTH_LONG).show();
                resetPicture();
            }
        }
    }

}
