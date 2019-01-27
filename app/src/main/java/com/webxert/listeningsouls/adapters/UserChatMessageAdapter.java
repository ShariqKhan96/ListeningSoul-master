package com.webxert.listeningsouls.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.webxert.listeningsouls.R;
import com.webxert.listeningsouls.common.Constants;
import com.webxert.listeningsouls.models.MessageModel;

import java.util.ArrayList;

/**
 * Created by hp on 1/16/2019.
 */

public class UserChatMessageAdapter extends RecyclerView.Adapter<UserChatMessageAdapter.MyVH> {

    private final int LEFT_LAYOUT = 0;
    private final int RIGHT_LAYOUT = 1;
    private ArrayList<MessageModel> arrayList = new ArrayList<>();
    private Context context;
    SharedPreferences preferences;

    public UserChatMessageAdapter(ArrayList<MessageModel> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
        preferences = this.context.getSharedPreferences(Constants.SH_PREFS, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public MyVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == LEFT_LAYOUT)
            return new MyVH(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.left_view, viewGroup, false));
        else
            return new MyVH(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.right_view, viewGroup, false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyVH myVH, int i) {

        myVH.sent_time.setText(arrayList.get(i).getSent_time());
        myVH.message.setText(arrayList.get(i).getMessage());
        if (arrayList.get(i).getIs_admin().equals("1")) {
            myVH.profile_image.setVisibility(View.VISIBLE);
            myVH.personName.setText(arrayList.get(i).getEmail().substring(0, 1).toUpperCase());
        } else
            myVH.profile_image.setVisibility(View.GONE);
//
//        if (i == arrayList.size() - 1) {
//            if (arrayList.get(i).is_seen) {
//                myVH.is_seen.setText("Seen");
//            } else
//                myVH.is_seen.setVisibility(View.GONE);
//        }


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyVH extends RecyclerView.ViewHolder {

        TextView message;
        RelativeLayout profile_image;
        TextView personName;
        TextView sent_time;
        TextView is_seen;

        public MyVH(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.message);
            profile_image = itemView.findViewById(R.id.profile_image);
            personName = itemView.findViewById(R.id.personName);
            sent_time = itemView.findViewById(R.id.sent_time);
            is_seen = itemView.findViewById(R.id.message_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (arrayList.get(position).getIs_admin().equals("0")) {
            return RIGHT_LAYOUT;
        } else return LEFT_LAYOUT;
    }
}
