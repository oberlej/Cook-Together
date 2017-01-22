package com.cooktogether.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.model.Conversation;
import com.cooktogether.model.Message;

import java.util.ArrayList;

/**
 * Created by hela on 06/01/17.
 */

public class ChatListViewHolder extends RecyclerView.ViewHolder{
    public TextView title;
    public TextView snap;
    public TextView time;
    public TextView count;

    public ChatListViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.item_chat_list_title);
        snap = (TextView) itemView.findViewById(R.id.item_chat_list_snap);
        time = (TextView) itemView.findViewById(R.id.item_chat_list_time);
        count = (TextView) itemView.findViewById(R.id.item_chat_list_count);
    }

    public void bindToPost(Conversation conversation) {
        title.setText(conversation.getTitle());
        ArrayList<Message> m = conversation.getMessages();
        if(m.isEmpty())
            snap.setText("No messages");
        else {
            Message lastMessage = m.get(m.size() - 1);
            snap.setText(lastMessage.getContent());
            time.setText( lastMessage.getDate().toString());

            if (conversation.getUnread() > 0) {
                count.setText(String.valueOf(conversation.getUnread()));
                count.setVisibility(View.VISIBLE);
            } else {
                count.setVisibility(View.GONE);
            }
        }


    }
}
