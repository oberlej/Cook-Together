package com.cooktogether.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Conversation;
import com.cooktogether.model.Message;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hela on 06/01/17.
 */

public class ChatFragment extends Fragment {

    private FirebaseListAdapter<Message> mAdapter;
    private ListView mList;
    private Button mSendButton;
    private EditText mMessage;
    protected HomeActivity mParent;

    private String mConversationKey = null;

    private List<String> mUsersKeys;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    public ChatFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
//        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mParent = (HomeActivity) getActivity();

        mParent.checkIsConnected();

        // Set up Layout Manager, reverse layout
//        mManager = new LinearLayoutManager(getContext());
//        mManager.setReverseLayout(true);
//        mManager.setStackFromEnd(true);

        initFields(view);

        loadConversation();
        return view;
    }

    private void loadConversation() {

        mParent.getDB().child("user-conversations").child(mParent.getUid())
                .child(mConversationKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Conversation conversation = Conversation.parseSnapshot(dataSnapshot);
//                mTitle.setText(conversation.getTitle());
                mUsersKeys = conversation.getUsersKeys();

                //to make sure the current user Id is always the first in the list
                mUsersKeys.remove(mParent.getUid());
                mUsersKeys.add(0, mParent.getUid());


                mAdapter = new FirebaseListAdapter<Message>(getActivity(), Message.class, R.layout.item_chat, dataSnapshot.child("messages").getRef()) {
                    @Override
                    protected void populateView(View v, Message model, int position) {
                        // Get references to the views of message.xml
                        TextView messageText = (TextView) v.findViewById(R.id.message_text);
                        TextView messageTime = (TextView) v.findViewById(R.id.message_time);

                        // Set their text
                        messageText.setText(model.getContent());

                        // Format the date before showing it
                        messageTime.setText(model.getDate().toString());

                        if (!model.getSenderId().equals(mParent.getUid())) {
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messageText.getLayoutParams();
                            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                            messageText.setLayoutParams(params);
                            messageText.setBackgroundResource(R.drawable.bg_bubble_white);

                            params = (RelativeLayout.LayoutParams) messageTime.getLayoutParams();
                            params.addRule(RelativeLayout.ALIGN_LEFT, R.id.message_text);
                            messageTime.setLayoutParams(params);
                        }
                    }
                };

                mList.setAdapter(mAdapter);
//
//                // Set up FirebaseRecyclerAdapter with the Query
//                mAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(Message.class, R.layout.item_message, MessageViewHolder.class, ) {
//
//                    @Override
//                    protected Message parseSnapshot(DataSnapshot snapshot) {
//                        return Message.parseSnapshot(snapshot);
//                    }
//
//                    @Override
//                    protected void populateViewHolder(final MessageViewHolder viewHolder, final Message model, final int position) {
//                        final DatabaseReference messageRef = getRef(position);
//
//                        // Set click listener for the whole meal view
//                        final String messageKey = messageRef.getKey();
//                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                // Launch Post date of message
//                                TextView dateView = (TextView) v.findViewById(R.id.msg_date);
//                                if (dateView.getVisibility() == View.GONE) {
//                                    dateView.setText(model.getDate().toString());
//                                    dateView.setVisibility(View.VISIBLE);
//                                } else
//                                    dateView.setVisibility(View.GONE);
//                                Toast.makeText(getContext(), "Message clicked", Toast.LENGTH_LONG).show();
//                            }
//                        });
//
//                        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int choice) {
//                                    switch (choice) {
//                                        case DialogInterface.BUTTON_POSITIVE:
//                                            messageRef.removeValue().addOnFailureListener(failureListener);
//                                            Toast.makeText(getContext(), "Message has been deleted", Toast.LENGTH_LONG).show();
//                                            break;
//                                        case DialogInterface.BUTTON_NEGATIVE:
//                                            break;
//                                    }
//                                }
//                            };
//
//                            @Override
//                            public boolean onLongClick(View v) {
//
//                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                                String message = "Are you sure you want to delete your version of messages ?";
//                                builder.setMessage(message)
//                                        .setPositiveButton("Yes", dialogClickListener)
//                                        .setNegativeButton("No", dialogClickListener).show();
//                                return false;
//                            }
//                        });
//                        // Bind Post to ViewHolder, setting OnClickListener for the star button
//                        viewHolder.bindToPost(model, mParent.getUid());
//                    }
//                };
//                mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//                    @Override
//                    public void onItemRangeInserted(int positionStart, int itemCount) {
//                        super.onItemRangeInserted(positionStart, itemCount);
////                        int messageCount = mAdapter.getItemCount();
////                        int lastVisiblePosition = mManager.findLastCompletelyVisibleItemPosition();
////                        // If the recycler view is initially being loaded or the
////                        // user is at the bottom of the list, scroll to the bottom
////                        // of the list to show the newly added message.
////                        if (lastVisiblePosition == -1 ||
////                                (positionStart >= (messageCount - 1) &&
////                                        lastVisiblePosition == (positionStart - 1))) {
////                            mRecycler.scrollToPosition(positionStart);
////                        }
//
////                        mRecycler.getLayoutManager().smoothScrollToPosition(mRecycler, null, mAdapter.getItemCount() - 1);
//                    }
//                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load Conversation.", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void initFields(View view) {
        mList = (ListView) view.findViewById(R.id.chat_list);
        mSendButton = (Button) view.findViewById(R.id.chat_send);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMessage.getText().toString().trim().isEmpty()) {
                    mMessage.setError(getString(R.string.message_empty));
                    requestFocus(mMessage);
                    return;
                }
                sendMessage(view);
            }
        });


        mConversationKey = mParent.getConversationKey();
//        mTitle = (TextView) view.findViewById(R.id.conversation_title);
        mMessage = (EditText) view.findViewById(R.id.chat_text_input);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public void sendMessage(View view) {
        Calendar calendar = Calendar.getInstance();
        Message m = new Message(mParent.getUid(), mMessage.getText().toString(), calendar.getTime());

        //in case its the first message (once created or after been deleted
        Conversation newConv = new Conversation("Title", mConversationKey, mUsersKeys);
        HashMap<String, Object> convMap = newConv.toHashMap();
        convMap.remove("messages"); //to not delete previous messages if any
        mParent.getDB().child("user-conversations").child(mUsersKeys.get(1)).child(mConversationKey).getRef().updateChildren(convMap);

        //updating messages of the conversation
        mParent.getDB().child("user-conversations").child(mUsersKeys.get(1)).child(mConversationKey).child("messages").push().setValue(m);


        mParent.getDB().child("user-conversations").child(mUsersKeys.get(0)).child(mConversationKey).child("messages").push().setValue(m);

        //clearingthe edit text field
        mMessage.setText("");
    }

    private OnFailureListener failureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(getContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
    };
}


