package com.cooktogether.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.helpers.AbstractBaseFragment;
import com.cooktogether.helpers.DividerItemDecoration;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Conversation;
import com.cooktogether.viewholder.ChatListViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import static android.R.attr.verticalDivider;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by hela on 06/01/17.
 */

public class ChatListFragment extends AbstractBaseFragment {
    private FirebaseRecyclerAdapter<Conversation, ChatListViewHolder> mAdapter;
    protected RecyclerView mRecycler;
    protected LinearLayoutManager mManager;

//    private TextView mEmptyList;

    public ChatListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        init(view);
        ((HomeActivity) mParent).hideKeyboard(getContext());
        return view;
    }

    @Override
    protected void init(View view) {
        mParent = (HomeActivity) getActivity();
        mParent.checkIsConnected();
        mParent.getSupportActionBar().setTitle(R.string.my_messages);
//        mEmptyList = (TextView) view.findViewById(R.id.chat_list_empty_list);

        mRecycler = (RecyclerView) view.findViewById(R.id.chat_list_content);
        mManager = new LinearLayoutManager(getContext());

        mRecycler.setHasFixedSize(true);
//        DividerItemDecoration decration = new DividerItemDecoration(mRecycler.getContext(), mManager.getOrientation());
//        decration.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.line_divider));
//        mRecycler.addItemDecoration(decration);
        ;
        // Set up Layout Manager, reverse layout

//        mManager.setReverseLayout(true);
//        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);
        mRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.line_divider));


        // Set up FirebaseRecyclerAdapter with the Query
        Query conversationsQuery = getQuery(getDB());
        queryAdapter(conversationsQuery);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public void queryAdapter(Query conversationsQuery) {
        mAdapter = new FirebaseRecyclerAdapter<Conversation, ChatListViewHolder>(Conversation.class, R.layout.item_chat_list, ChatListViewHolder.class, conversationsQuery) {

            @Override
            protected Conversation parseSnapshot(DataSnapshot snapshot) {
                return Conversation.parseSnapshot(snapshot);
            }

            @Override
            protected void populateViewHolder(final ChatListViewHolder viewHolder, final Conversation model, final int position) {
//                if (mEmptyList.getVisibility() == View.VISIBLE) {
//                    mEmptyList.setVisibility(View.GONE);
//                }
                final DatabaseReference conversationRef = getRef(position);
                // Set click listener for the whole meal view
                final String conversationKey = conversationRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch Conversation Fragment
                        ((HomeActivity) mParent).goToConversation(conversationKey);
                    }
                });

                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int choice) {
                            switch (choice) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    conversationRef.removeValue().addOnFailureListener(failureListener);
                                    Toast.makeText(getContext(), "Conversation " + model.getTitle() + " has been deleted", Toast.LENGTH_LONG).show();
//                                    mEmptyList.setVisibility(View.VISIBLE);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    @Override
                    public boolean onLongClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        String message = "Are you sure you want to delete your version of the conversation ?";
                        builder.setMessage(message)
                                .setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                        return false;
                    }
                });

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model);
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child(getString(R.string.db_user_conversations)).child(mParent.getUid()).orderByChild(getString(R.string.db_rank));
    }

    private OnFailureListener failureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(getContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
    };
}

