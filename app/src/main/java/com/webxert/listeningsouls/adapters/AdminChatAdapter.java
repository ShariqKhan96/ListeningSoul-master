package com.webxert.listeningsouls.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.webxert.listeningsouls.R;
import com.webxert.listeningsouls.common.Common;
import com.webxert.listeningsouls.models.MessageModel;

import java.util.ArrayList;
import java.util.List;

public class AdminChatAdapter extends RecyclerView.Adapter<AdminChatAdapter.myVH> {

    List<MessageModel> messageModels = new ArrayList<>();
    Context context;
    private final int LEFT_LAYOUT = 0;
    private final int RIGHT_LAYOUT = 1;


    public AdminChatAdapter(List<MessageModel> messageModels, Context context) {
        this.messageModels = messageModels;
        this.context = context;
    }

    class myVH extends RecyclerView.ViewHolder {
        TextView personName;
        TextView message;


        public myVH(@NonNull View itemView) {
            super(itemView);
            personName = itemView.findViewById(R.id.personName);
            message = itemView.findViewById(R.id.message);

        }
    }

    @NonNull
    @Override
    public AdminChatAdapter.myVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == RIGHT_LAYOUT) {
            return new myVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.right_admin_layout, parent, false));
        } else
            return new myVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.left_admin_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AdminChatAdapter.myVH myVH, int adapterPosition) {

        MessageModel message = messageModels.get(myVH.getAdapterPosition());
        myVH.message.setText(message.getMessage());
        myVH.personName.setText(Common.getPersonName(message.getId()));


    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messageModels.get(position).getId())) {
            return RIGHT_LAYOUT;
        } else
            return LEFT_LAYOUT;
    }
}
