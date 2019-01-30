package com.webxert.listeningsouls.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.webxert.listeningsouls.MyGlideProvider;
import com.webxert.listeningsouls.R;
import com.webxert.listeningsouls.common.Constants;
import com.webxert.listeningsouls.models.MessageModel;

import java.util.ArrayList;

/**
 * Created by hp on 1/16/2019.
 */

public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.MyVH> {

    private final int LEFT_LAYOUT = 0;
    private final int RIGHT_LAYOUT = 1;
    private ArrayList<MessageModel> arrayList = new ArrayList<>();
    private Context context;
    SharedPreferences preferences;

    public ChatMessagesAdapter(ArrayList<MessageModel> arrayList, Context context) {
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
    public void onBindViewHolder(@NonNull final MyVH myVH, int i) {

        if (arrayList.get(i).getMessage_type().equals("text")) {
            myVH.image_layout.setVisibility(View.GONE);
            myVH.progressBar.setVisibility(View.GONE);
            myVH.message.setVisibility(View.VISIBLE);
            myVH.message.setText(arrayList.get(i).getMessage());
            myVH.image.setVisibility(View.GONE);
        } else if (arrayList.get(i).getMessage_type().equals("image")) {
            myVH.message.setVisibility(View.GONE);
            myVH.image_layout.setVisibility(View.VISIBLE);
            myVH.progressBar.setVisibility(View.VISIBLE);
            myVH.image.setVisibility(View.VISIBLE);
            Log.e("ImageUrl",arrayList.get(i).getImage_url() );
            Glide.with(context)
                    .load(arrayList.get(i).getImage_url())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("onLoadFailed","onLoadFailed");
                            myVH.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.e("onResourceReady","onResourceReady");
                            myVH.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(myVH.image);
        } else {
            myVH.image.setImageDrawable(null);
        }

        //   Log.e("time", arrayList.get(i).getSent_time());
        myVH.sent_time.setText(arrayList.get(i).sent_time);
        if (arrayList.get(i).getIs_admin().equals("0")) {
            myVH.profile_image.setVisibility(View.VISIBLE);
            myVH.personName.setText(arrayList.get(i).getEmail().substring(0, 1).toUpperCase());
        } else
            myVH.profile_image.setVisibility(View.GONE);


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
        ImageView image;
        RelativeLayout image_layout;
        ProgressBar progressBar;


        public MyVH(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.message);
            profile_image = itemView.findViewById(R.id.profile_image);
            personName = itemView.findViewById(R.id.personName);
            sent_time = itemView.findViewById(R.id.sent_time);
            is_seen = itemView.findViewById(R.id.message_seen);
            image = itemView.findViewById(R.id.image);
            image_layout = itemView.findViewById(R.id.image_layout);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (arrayList.get(position).getIs_admin().equals("0")) {
            return LEFT_LAYOUT;
        } else return RIGHT_LAYOUT;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
