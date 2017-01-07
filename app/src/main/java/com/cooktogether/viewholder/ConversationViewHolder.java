package com.cooktogether.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.model.Conversation;

/**
 * Created by hela on 06/01/17.
 */

public class ConversationViewHolder extends RecyclerView.ViewHolder{
    public TextView title;
    public TextView snap;

    public ConversationViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.conversation_title);
        snap = (TextView) itemView.findViewById(R.id.conversation_snap);
    }

    public void bindToPost(Conversation conversation) {
        title.setText(conversation.getTitle());
        snap.setText(conversation.getLastMessage());
    }
}
