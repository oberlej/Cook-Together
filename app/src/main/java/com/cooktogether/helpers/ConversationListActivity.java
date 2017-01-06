package com.cooktogether.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.model.Conversation;
import com.cooktogether.viewholder.ConversationViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by hela on 06/01/17.
 */

public class ConversationListActivity extends AbstractBaseActivity {
    private FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> mAdapter;
    protected RecyclerView mRecycler;
    protected LinearLayoutManager mManager;

    public ConversationListActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsConnected();
        setContentView(R.layout.activity_conversation_list);
        mRecycler = (RecyclerView) findViewById(R.id.conversations_list_rcv);
        mRecycler.setHasFixedSize(true);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getApplicationContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query conversationsQuery = getQuery(getDB());

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
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                        intent.putExtra("key", conversationKey);
                        intent.putExtra("title", model.getTitle());
                        startActivity(intent);
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
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public Query getQuery(DatabaseReference databaseReference){
        return databaseReference.child("conversations");
    }
}

