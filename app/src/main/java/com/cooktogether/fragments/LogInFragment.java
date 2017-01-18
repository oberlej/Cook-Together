package com.cooktogether.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.helpers.AbstractBaseFragment;
import com.cooktogether.mainscreens.AuthenticationActivity;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Review;
import com.cooktogether.model.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LogInFragment extends AbstractBaseFragment {
    protected EditText emailEditText;
    protected EditText passwordEditText;
    protected Button logInButton;
    protected TextView signUpTextView;

    public static LogInFragment newInstance() {
        return new LogInFragment();
    }

    public LogInFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_in, container, false);
        init(view);
        return view;
    }

    @Override
    protected void init(View view) {
        mParent = (AuthenticationActivity) getActivity();
        emailEditText = (EditText) view.findViewById(R.id.login_email);
        passwordEditText = (EditText) view.findViewById(R.id.login_pw);
        logInButton = (Button) view.findViewById(R.id.login_btn);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                email = email.trim();
                password = password.trim();

                if (email.isEmpty() || password.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(R.string.login_error_message)
                            .setTitle(R.string.login_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    getAuth().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        checkIfFirstTimeForUser(false);
                                        Intent intent = new Intent(getContext(), HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setMessage(task.getException().getMessage())
                                                .setTitle(R.string.login_error_title)
                                                .setPositiveButton(android.R.string.ok, null);
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            });
                }
            }
        });

        // Initialize Facebook Login button
        LoginButton loginButtonFB = (LoginButton) view.findViewById(R.id.login_fb_btn);
        loginButtonFB.setReadPermissions("email", "public_profile");
        loginButtonFB.registerCallback(((AuthenticationActivity) getActivity()).getCallbackManager(), new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(getContext(), "Connection canceled.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getContext(), "Authentication failed. " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        getAuth().signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_LONG).show();
                        } else {
                            checkIfFirstTimeForUser(true);
                        }
                    }
                });
    }

    private void checkIfFirstTimeForUser(final boolean facebook) {
        //check if it is the first time the user connects to our app
        getDB().child("users").child(getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //user has already connected once
                } else {
                    //first time => create user entry
                    FirebaseUser cu = getCurrentUser();
                    User newUser = new User(cu.getUid(), cu.getDisplayName(), cu.getEmail(), "", "", facebook, facebook, new ArrayList<Review>());
                    getDB().child("users").child(cu.getUid()).setValue(newUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Something went wrong. Please logout and try logging in again.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
