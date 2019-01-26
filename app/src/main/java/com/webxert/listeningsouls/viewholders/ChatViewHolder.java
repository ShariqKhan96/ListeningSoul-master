package com.webxert.listeningsouls.viewholders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.library.bubbleview.BubbleTextView;
import com.webxert.listeningsouls.R;

/**
 * Created by hp on 12/10/2018.
 */


public class ChatViewHolder extends RecyclerView.ViewHolder {
   public TextView chat_with;
   public RelativeLayout profile_image;
   public TextView personName;
   public TextView seenMessages;
   public TextView messageTime;



    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);

        chat_with = itemView.findViewById(R.id.chat_with);
        profile_image = itemView.findViewById(R.id.profile_image);
        personName = itemView.findViewById(R.id.personName);
        seenMessages = itemView.findViewById(R.id.message_seen);
        messageTime = itemView.findViewById(R.id.chat_time);
    }

}


