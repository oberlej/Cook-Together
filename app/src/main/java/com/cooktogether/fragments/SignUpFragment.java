package com.cooktogether.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.cooktogether.R;
import com.cooktogether.helpers.AbstractBaseActivity;
import com.cooktogether.helpers.AbstractBaseFragment;
import com.cooktogether.mainscreens.AuthenticationActivity;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Review;
import com.cooktogether.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SignUpFragment extends AbstractBaseFragment {

    protected EditText passwordEditText;
    protected EditText emailEditText;
    protected Button signUpButton;

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    public SignUpFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        init(view);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void init(View view) {

        mParent = (AbstractBaseActivity) getActivity();

        passwordEditText = (EditText) view.findViewById(R.id.signup_pw);
        emailEditText = (EditText) view.findViewById(R.id.signup_email);

        signUpButton = (Button) view.findViewById(R.id.signup_btn);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordEditText.getText().toString();
                String email = emailEditText.getText().toString();

                password = password.trim();
                email = email.trim();

                if (password.isEmpty() || email.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(R.string.signup_error_message)
                            .setTitle(R.string.signup_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    getAuth().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        //create user in db
                                        FirebaseUser cu = getCurrentUser();
                                        User newUser = new User(cu.getUid(), cu.getDisplayName(), cu.getEmail(), "", "", false, false, new ArrayList<Review>());
                                        getDB().child("users").child(cu.getUid()).setValue(newUser);

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
    }

}
