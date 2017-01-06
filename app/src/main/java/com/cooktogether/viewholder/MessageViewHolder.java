package com.cooktogether.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.model.Message;

/**
 * Created by hela on 06/01/17.
 */

public class MessageViewHolder extends RecyclerView.ViewHolder{
    public TextView content;

    public MessageViewHolder(View itemView) {
        super(itemView);
        content = (TextView) itemView.findViewById(R.id.message_content);
    }

    public void bindToPost(Message message) {
        content.setText(message.getContent());
    }
}
