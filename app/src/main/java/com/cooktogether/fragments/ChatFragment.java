package com.cooktogether.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.cooktogether.helpers.AbstractBaseFragment;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Conversation;
import com.cooktogether.model.Message;
import com.cooktogether.model.User;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hela on 06/01/17.
 */

public class ChatFragment extends AbstractBaseFragment {

    private FirebaseListAdapter<Message> mAdapter;
    private ListView mList;
    private Button mSendButton;
    private EditText mMessage;

    private Conversation mConversation = null;
    private String mConversationKey = null;

    private List<String> mUsersKeys;

    private DatabaseReference mConversationRef;
    private ValueEventListener mConversationListener;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    public ChatFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mParent = (HomeActivity) getActivity();
        mParent.checkIsConnected();
        init(view);
        loadConversation();
        return view;
    }

    @Override
    protected void init(View view) {
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

        mConversationKey = ((HomeActivity) mParent).getConversationKey();
        mMessage = (EditText) view.findViewById(R.id.chat_text_input);

        mConversationRef = getDB().child(getString(R.string.db_user_conversations)).child(mParent.getUid()).child(mConversationKey);

        mAdapter = new FirebaseListAdapter<Message>(getActivity(), Message.class, R.layout.item_chat, mConversationRef.child(getString(R.string.db_messages))) {
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
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                    messageText.setLayoutParams(params);
                    messageText.setBackgroundResource(R.drawable.bg_bubble_white);

                    params = (RelativeLayout.LayoutParams) messageTime.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_LEFT, R.id.message_text);
                    messageTime.setLayoutParams(params);
                } else {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messageText.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    messageText.setLayoutParams(params);
                    messageText.setBackgroundResource(R.drawable.bg_bubble_gray);

                    params = (RelativeLayout.LayoutParams) messageTime.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.message_text);
                    messageTime.setLayoutParams(params);
                }
            }

        };
        mList.setAdapter(mAdapter);

        mConversationListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Boolean firstTime = mConversation == null;
                Conversation conversation = Conversation.parseSnapshot(dataSnapshot);
                mConversation = conversation;
                //set unread to 0
                if (conversation.getUnread() > 0) {
                    HashMap<String, Object> convMap = new HashMap<>();
                    convMap.put(getString(R.string.db_unread), 0);
                    dataSnapshot.getRef().updateChildren(convMap);

                    //set nb conv unread -1
                    getDB().child(getString(R.string.db_users)).child(getUid()).child(getString(R.string.db_unread)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int unread = 1;
                            if (dataSnapshot != null && dataSnapshot.exists()) {
                                unread = ((Long) dataSnapshot.getValue()).intValue();
                                if (unread < 1) unread = 1;
                            }
                            getDB().child(getString(R.string.db_users)).child(getUid()).child(getString(R.string.db_unread)).setValue(unread - 1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                //set up only done first time listener is fired
                if (firstTime) {
                    mParent.getSupportActionBar().setTitle(conversation.getTitle());
                    mUsersKeys = conversation.getUsersKeys();

                    //to make sure the current user Id is always the first in the list
                    mUsersKeys.remove(mParent.getUid());
                    mUsersKeys.add(0, mParent.getUid());

                    getDB().child(getString(R.string.db_users)).child(mUsersKeys.get(1)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final User user = User.parseSnapshot(dataSnapshot);
                            //CLICKABLE ACTION BAR
                            final View actionBar = mParent.findViewById(R.id.toolbar_main);
                            actionBar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Launch Meal Details Fragment
                                    //to get the user pic
                                    actionBar.setClickable(false);
                                    ((HomeActivity) mParent).setToVisit(user);
                                    ((HomeActivity) mParent).goToMeal(mConversationKey);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), R.string.fail_load_conv, Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        //add listeners
        mConversationRef.addValueEventListener(mConversationListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove post value event listener
        if (mConversationListener != null) {
            mConversationRef.removeEventListener(mConversationListener);
        }

        // Clean up comments listener
        mAdapter.cleanup();
    }

    private void loadConversation() {
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
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
        HashMap<String, Object> convMap = mConversation.toHashMap();
        convMap.remove(getString(R.string.db_messages)); //to not delete previous messages if any
        convMap.remove(getString(R.string.db_unread)); //to not delete previous messages if any
        getDB().child(getString(R.string.db_user_conversations)).child(mUsersKeys.get(1)).child(mConversationKey).getRef().updateChildren(convMap);

        //updating messages of the conversation
        getDB().child(getString(R.string.db_user_conversations)).child(mUsersKeys.get(1)).child(mConversationKey).child(getString(R.string.db_messages)).push().setValue(m);
        //update unread
        getDB().child(getString(R.string.db_user_conversations)).child(mUsersKeys.get(1)).child(mConversationKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Conversation receiverConv = Conversation.parseSnapshot(dataSnapshot);
                if (receiverConv.getUnread() < 1) {
                    //first unread message for this conversation
                    getDB().child(getString(R.string.db_users)).child(mUsersKeys.get(1)).child(getString(R.string.db_unread)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int unread = 0;
                            if (dataSnapshot != null && dataSnapshot.exists()) {
                                unread = ((Long) dataSnapshot.getValue()).intValue();
                            }
                            getDB().child(getString(R.string.db_users)).child(mUsersKeys.get(1)).child(getString(R.string.db_unread)).setValue(unread + 1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                HashMap<String, Object> conversation = new HashMap<>();
                conversation.put(getString(R.string.db_unread), receiverConv.getUnread() + 1);
                dataSnapshot.getRef().updateChildren(conversation);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        getDB().child(getString(R.string.db_user_conversations)).child(mUsersKeys.get(0)).child(mConversationKey).child(getString(R.string.db_messages)).push().setValue(m);

        //update rank so that the conv are displayed on top
        getDB().child(getString(R.string.db_user_conversations)).child(mUsersKeys.get(0)).child(mConversationKey).child(getString(R.string.db_rank)).setValue(0 - calendar.getTime().getTime());
        getDB().child(getString(R.string.db_user_conversations)).child(mUsersKeys.get(1)).child(mConversationKey).child(getString(R.string.db_rank)).setValue(0 - calendar.getTime().getTime());
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