package com.webxert.listeningsouls.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.webxert.listeningsouls.ChatActivity;
import com.webxert.listeningsouls.MainActivity;
import com.webxert.listeningsouls.R;
import com.webxert.listeningsouls.common.Common;
import com.webxert.listeningsouls.common.Constants;
import com.webxert.listeningsouls.interfaces.LogoutListener;
import com.webxert.listeningsouls.interfaces.UserIdentityListener;
import com.webxert.listeningsouls.models.ChatModel;
import com.webxert.listeningsouls.models.User;
import com.webxert.listeningsouls.utils.Utils;
import com.webxert.listeningsouls.viewholders.ChatViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserChatFragment extends Fragment {


    public static UserChatFragment instance;
    RecyclerView recyclerView;
    ImageView details;
    FirebaseRecyclerAdapter<ChatModel, ChatViewHolder> adapter;
    int count = 0;
    ProgressDialog dialog;
    boolean message_found;
    RelativeLayout chat_admin_layout;
    LogoutListener logoutListener;
    UserIdentityListener userIdentityListener;


    public static UserChatFragment getInstance() {
        if (instance == null)
            instance = new UserChatFragment();
        return instance;
    }

    public UserChatFragment() {

    }

    public void setLogoutListener(LogoutListener logoutListener) {
        this.logoutListener = logoutListener;
    }

    public void setUserIdentityListener(UserIdentityListener userIdentityListener) {
        this.userIdentityListener = userIdentityListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.admin_chat_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.logout == item.getItemId())
            logoutListener.onLogout();
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_chat, container, false);
        chat_admin_layout = view.findViewById(R.id.chat_admin_layout);

        chat_admin_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAdminFragment();
            }
        });
        recyclerView = view.findViewById(R.id.chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.HORIZONTAL));
        getData();

        return view;

    }

    private void openAdminFragment() {
        AdminChatFragment adminChatFragment = new AdminChatFragment();
        adminChatFragment.setLogoutListener(logoutListener);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_layout, adminChatFragment)
                .addToBackStack(null)
                .commit();
    }

    private void getData() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!message_found) {
                    Toast.makeText(getContext(), "No chats found with users or slow connection!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

            }
        }, 6000);

        dialog = Utils.getChatProgressDialog(getContext());
        dialog.show();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("chats");
        dbRef.keepSynced(true);
        //yahan query bhi dfferent hogi users wali nahi chalygi
        Query query = dbRef.orderByChild("timestamp");
        /*chats.child(wo saray log ki id jihun nai chat ki hai admin sai)*/

        FirebaseRecyclerOptions<ChatModel> options = new FirebaseRecyclerOptions.Builder<ChatModel>()
                .setQuery(query, ChatModel.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<ChatModel, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull final ChatModel model) {

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
                message_found = true;
                holder.messageTime.setText(format.format(model.getDate().getTime()));
                if (dialog.isShowing())
                    dialog.dismiss();
                holder.chat_with.setText(Common.getPersonName(model.getId()));
                holder.personName.setText(model.getWith().substring(0, 1).toUpperCase());
                if (!model.isSeen())
                    holder.seenMessages.setVisibility(View.VISIBLE);
                else holder.seenMessages.setVisibility(View.INVISIBLE);

                holder.details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDetailsDialog(model);
                    }
                });
//                if (model.getAssignedTo().equals("None"))
//                    holder.assignedTo.setText(new StringBuilder("Assigned To: None"));
//                else
//                    holder.assignedTo.setText(new StringBuilder("Assigned To: ").append(Common.getPersonName(model.getAssignedTo())));


                //avatar imageview
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openChatFragment(model.getId(), model.getWith());
                    }
                });

            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new ChatViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_view, viewGroup, false));
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);


    }

    private void showDetailsDialog(ChatModel model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = getLayoutInflater().inflate(R.layout.details_dialog, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        final TextView messageCount, assignTo, blockStatus;
        Button dismiss;

        assignTo = view.findViewById(R.id.assign_to);
        dismiss = view.findViewById(R.id.dismiss);
        blockStatus = view.findViewById(R.id.block_status);
        messageCount = view.findViewById(R.id.message_count);

        if (model.getAssignedTo().equals("None")) assignTo.setText("None");
        else
            assignTo.setText(Common.getPersonName(model.getAssignedTo()));
        if (Common.checkBlockStatus(model.getId())) {
            blockStatus.setTextColor(getResources().getColor(R.color.errorColor));
            blockStatus.setText("Blocked");
        } else {
            blockStatus.setTextColor(getResources().getColor(R.color.successColor));
            blockStatus.setText("Not Blocked");
        }
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        FirebaseDatabase.getInstance().getReference("Messages").child(model.getId()).child(Constants.DOMAIN_NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageCount.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openChatFragment(String id, String email) {

        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("email", email);
        startActivity(intent);


//        Bundle bundle = new Bundle();
//        bundle.putString("id", id);
//        bundle.putString("email", email);
//
//        ChatFragment chatFragment = new ChatFragment();
//        chatFragment.setLogoutListener(logoutListener);
//        chatFragment.setUserIdentityListener(userIdentityListener);
//        chatFragment.setArguments(bundle);
//        getActivity().getSupportFragmentManager().beginTransaction()
//                .replace(R.id.admin_layout, chatFragment)
//                .addToBackStack(null)
//                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null)
            adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }
}
