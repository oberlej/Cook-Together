package com.cooktogether.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.model.Conversation;
import com.cooktogether.model.Message;
import com.cooktogether.viewholder.MessageViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hela on 06/01/17.
 */

public class ConversationActivity extends AbstractBaseActivity {

    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mAdapter;
    protected RecyclerView mRecycler;
    protected LinearLayoutManager mManager;

    private String mConversationKey = null;
    private String mTitle;
    private int nbrMessages;
    private EditText newMessage;
    private List<String> usersKeys;

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

        loadConversation();
    }

    private void loadConversation() {
        TextView title = (TextView) findViewById(R.id.conversation_title);
        title.setText(mTitle);

        getDB().child("user-conversations").child(getUid()).child(mConversationKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Conversation conversation = Conversation.parseSnapshot(dataSnapshot);
                //mTitle.setText(conversation.getTitle());
                usersKeys = conversation.getUsersKeys();

                //to make sure the current user Id is always the first in the list
                usersKeys.remove(getUid());
                usersKeys.add(0,getUid());

                nbrMessages = conversation.getMessages().size();
                // Set up FirebaseRecyclerAdapter with the Query
                mAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(Message.class, R.layout.item_message, MessageViewHolder.class, dataSnapshot.child("messages").getRef()) {

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
                        viewHolder.bindToPost(model, getUid());
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

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load meal.", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void initFields() {
        Intent intent = getIntent();
        if (intent.hasExtra("key")) {
            mConversationKey = intent.getStringExtra("key");
        }
        if (intent.hasExtra("title")) {
            mTitle = intent.getStringExtra("title");
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
        return databaseReference.child("user-conversations").child(getUid()).child(mConversationKey).child("messages");

    }
    public void sendMessage(View view){
        newMessage = (EditText) findViewById(R.id.text_message);
        Message m = new Message(getUid(),newMessage.getText().toString());

        //getDB().child("conversations").child(mConversationKey).child("messages").push().setValue(m);

        if(nbrMessages == 0) {
            Conversation newConv = new Conversation(mTitle, mConversationKey, usersKeys);
            getDB().child("user-conversations").child(usersKeys.get(1)).child(mConversationKey).setValue(newConv);
        }
        getDB().child("user-conversations").child(usersKeys.get(1)).child(mConversationKey).child("messages").push().setValue(m);

        getDB().child("user-conversations").child(usersKeys.get(0)).child(mConversationKey).child("messages").push().setValue(m);

        newMessage.setText("");

        Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();
    }
}


