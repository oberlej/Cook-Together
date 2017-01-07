package com.cooktogether.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.fragments.ProfileFragment;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Conversation;
import com.cooktogether.viewholder.ConversationViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by hela on 06/01/17.
 */

public class ConversationsListFragment extends Fragment {
    private FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> mAdapter;
    protected RecyclerView mRecycler;
    protected LinearLayoutManager mManager;

    protected HomeActivity mParent;


    public ConversationsListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversations_list, container, false);

        mParent = (HomeActivity) getActivity();

        mParent.checkIsConnected();

        mRecycler = (RecyclerView) view.findViewById(R.id.conversations_list_rcv);
        mRecycler.setHasFixedSize(true);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query conversationsQuery = getQuery(mParent.getDB());
        queryAdapter(conversationsQuery);

        mRecycler.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public void queryAdapter(Query conversationsQuery){
        mAdapter = new FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(Conversation.class, R.layout.item_conversation, ConversationViewHolder.class, conversationsQuery) {

            @Override
            protected Conversation parseSnapshot(DataSnapshot snapshot) {
                return Conversation.parseSnapshot(snapshot);
            }

            @Override
            protected void populateViewHolder(final ConversationViewHolder viewHolder, final Conversation model, final int position) {
                final DatabaseReference conversationRef = getRef(position);

                // Set click listener for the whole meal view
                final String conversationKey = conversationRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch Conversation Fragment
                        mParent.goToConversation(conversationKey);
                    }
                });

                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View v){
                        Toast.makeText(getApplicationContext(), "Are you sure you want to delete the conversation ?", Toast.LENGTH_LONG).show();

                        return false;
                    }
                });
                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model);
            }
        };
    }

    public Query getQuery(DatabaseReference databaseReference){
        return databaseReference.child("user-conversations").child(mParent.getUid());
    }
}

