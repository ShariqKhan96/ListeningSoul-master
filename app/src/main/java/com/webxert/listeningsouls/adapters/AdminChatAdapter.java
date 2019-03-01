package com.webxert.listeningsouls.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.webxert.listeningsouls.GlideApp;
import com.webxert.listeningsouls.PhotoActivity;
import com.webxert.listeningsouls.R;
import com.webxert.listeningsouls.common.Common;
import com.webxert.listeningsouls.models.MessageModel;

import org.w3c.dom.Text;

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
        TextView sent_time;
        ImageView image;
        RelativeLayout image_layout;
        ProgressBar progressBar;
        RelativeLayout normal_text_layout;


        public myVH(@NonNull View itemView) {
            super(itemView);
            personName = itemView.findViewById(R.id.personName);
            message = itemView.findViewById(R.id.message);
            sent_time = itemView.findViewById(R.id.sent_time);
            image = itemView.findViewById(R.id.image);
            image_layout = itemView.findViewById(R.id.image_layout);
            progressBar = itemView.findViewById(R.id.progressBar);
            normal_text_layout = itemView.findViewById(R.id.normal_text_layout);


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
    public void onBindViewHolder(@NonNull final AdminChatAdapter.myVH myVH, int adapterPosition) {

        final MessageModel message = messageModels.get(adapterPosition);
        myVH.personName.setText(Common.getPersonName(message.getId()));
        myVH.sent_time.setText(messageModels.get(adapterPosition).sent_time);

        if (message.getMessage_type().equals("image")) {
            myVH.image.setVisibility(View.VISIBLE);
            myVH.progressBar.setVisibility(View.VISIBLE);
            myVH.message.setVisibility(View.GONE);
            GlideApp.with(context)
                    .load(message.getImage_url())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("onLoadFailed", e.getMessage());
                            myVH.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.e("onResourceReady", "onResourceReady");
                            myVH.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(myVH.image);
        } else {
            myVH.image.setVisibility(View.GONE);
            myVH.progressBar.setVisibility(View.GONE);
            myVH.message.setVisibility(View.VISIBLE);
            myVH.message.setText(message.getMessage());

        }

        myVH.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getMessage_type().equals("image")) {
                    Log.e("Url", message.getImage_url());
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    View view = LayoutInflater.from(context).inflate(R.layout.photo_dialog, null);
//                    final ProgressBar progressBar = view.findViewById(R.id.progressBar);
//
//                   // progressBar.setVisibility(View.VISIBLE);
//                    ImageView photoView = view.findViewById(R.id.photo_view);
////                    Glide.with(context).load(arrayList.get(myVH.getAdapterPosition()).getImage_url())
////                            .listener(new RequestListener<Drawable>() {
////                                @Override
////                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
////                                   Log.e("Error", e.getMessage());
////                                    progressBar.setVisibility(View.GONE);
////                                    return false;
////                                }
////
////                                @Override
////                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
////                                    Log.e("onResourceReady","onResourceReady");
////
////                                    progressBar.setVisibility(View.GONE);
////                                    return false;
////                                }
////                            })
////                            .into(photoView);
//                    builder.setView(view);
//                    //photoView.setImageResource(R.mipmap.ic_launcher);
//                    //Picasso.get().load(arrayList.get(myVH.getAdapterPosition()).getImage_url()).into(photoView);
//
//                    builder.show();
                    Intent intent = new Intent(context, PhotoActivity.class);
                    intent.putExtra("url", message.getImage_url());
                    context.startActivity(intent);

                }
            }
        });
//        Log.e("time", messageModels.get(adapterPosition).sent_time);


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
