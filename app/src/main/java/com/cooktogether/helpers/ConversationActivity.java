package com.cooktogether.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.model.Conversation;
import com.cooktogether.model.Message;
import com.cooktogether.viewholder.MessageViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;

/**
 * Created by hela on 06/01/17.
 */

public class ConversationActivity extends AbstractBaseActivity {

    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mAdapter;
    protected RecyclerView mRecycler;
    protected LinearLayoutManager mManager;

    private String mConversationKey = null;
    private String mtitle;
    private ArrayList<Message> mMessages;
    private EditText newMessage;

    private boolean mIsUpdate = false;

    public ConversationActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsConnected();
        setContentView(R.layout.activity_conversation);

        mRecycler = (RecyclerView) findViewById(R.id.conversation_messages_list_rcv);
        mRecycler.setHasFixedSize(true);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getApplicationContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);
        initFields();
        // Set up FirebaseRecyclerAdapter with the Query
        Query conversationQuery = getQuery(getDB());

        mAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(Message.class, R.layout.item_message, MessageViewHolder.class, conversationQuery) {

            @Override
            protected Message parseSnapshot(DataSnapshot snapshot) {
                return Message.parseSnapshot(snapshot);
            }

            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder, final Message model, final int position) {
                final DatabaseReference messageRef = getRef(position);

                // Set click listener for the whole meal view
                final String messageKey = messageRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Toast.makeText(getApplicationContext(), "Message clicked", Toast.LENGTH_LONG).show();
                    }
                });

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model);
            }
        };
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = mAdapter.getItemCount();
                int lastVisiblePosition = mManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRecycler.scrollToPosition(positionStart);
                }
            }
        });

        mRecycler.setLayoutManager(mManager);
        mRecycler.setAdapter(mAdapter);
    }

    private void initFields() {
        Intent intent = getIntent();
        if (intent.hasExtra(getResources().getString(R.string.CONVERSATION_KEY))) {
            mConversationKey = intent.getStringExtra(getResources().getString(R.string.CONVERSATION_KEY));
            mIsUpdate = mConversationKey != null && !mConversationKey.isEmpty();
            //loadConversation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child("conversations").child(mConversationKey).child("messages");

    }
    public void sendMessage(View view){
        newMessage = (EditText) findViewById(R.id.text_message);
        Message m = new Message(getUid(),newMessage.getText().toString());

        getDB().child("conversations").child(mConversationKey).child("messages").push().setValue(m);

        newMessage.setText("");

        Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();
    }
}


