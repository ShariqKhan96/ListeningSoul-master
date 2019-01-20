package com.webxert.listeningsouls.fragments;


import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.webxert.listeningsouls.MainActivity;
import com.webxert.listeningsouls.R;
import com.webxert.listeningsouls.adapters.AdminChatAdapter;
import com.webxert.listeningsouls.adapters.UserChatMessageAdapter;
import com.webxert.listeningsouls.common.Constants;
import com.webxert.listeningsouls.models.ChatModel;
import com.webxert.listeningsouls.models.MessageModel;
import com.webxert.listeningsouls.models.SaverModel;
import com.webxert.listeningsouls.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


public class AdminChatFragment extends Fragment {

    RecyclerView messageRV;
    EditText message_text;
    ImageView submit_button;
    AdminChatAdapter adminChatAdapter;
    List<MessageModel> messages = new ArrayList<>();
    String email;
    boolean matched = false;
    SharedPreferences reader;
    boolean messages_found = false;
    ArrayList<SaverModel> arrayList = new ArrayList<>();

    public AdminChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        message_text = view.findViewById(R.id.message_text);
        messageRV = view.findViewById(R.id.user_message_list);
        submit_button = view.findViewById(R.id.submit_button);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        messageRV.setLayoutManager(linearLayoutManager);
        adminChatAdapter = new AdminChatAdapter(messages, getContext());
        messageRV.setAdapter(adminChatAdapter);
        reader = getContext().getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE);
        email = reader.getString(Constants.USER_EMAIL, "null");

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(message_text.getText().toString())) {
                    String message = message_text.getText().toString();
                    message_text.setText("");
                    FirebaseDatabase.getInstance().getReference("AdminMessages").push().setValue(new MessageModel(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "0", message, "1", FirebaseAuth.getInstance().getCurrentUser().getUid())).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            message_text.requestFocus();
                            displayMessages();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(MainActivity.class.getSimpleName(), e.getMessage());
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        });


        displayMessages();


        return view;
    }


    private void displayMessages() {

        final ProgressDialog dialog = Utils.getMessageProgressDialog(getContext());
        dialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                if (!messages_found)
                    Toast.makeText(getContext(), "No messages found!", Toast.LENGTH_SHORT).show();
            }
        }, 5000);

        DatabaseReference message_ref = FirebaseDatabase.getInstance().getReference("AdminMessages");

        message_ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                dialog.dismiss();
                messages_found = true;
                //Toast.makeText(MainActivity.this, ""+dataSnapshot.getChildrenCount(), Toast.LENGTH_SHORT).show();
                Map<String, String> map = (Map) dataSnapshot.getValue();
                SaverModel saverModel = new SaverModel(dataSnapshot.getKey(), map);
                Log.e("Key", dataSnapshot.getKey());
                Log.e("size", String.valueOf(arrayList.size()));
                if (arrayList.size() == 0 && messages.size() == 0) {
                    addNewMessage(arrayList, messages, adminChatAdapter, saverModel);
                }

                for (int i = 0; i < arrayList.size(); i++) {
                    SaverModel m = arrayList.get(i);
                    if (!dataSnapshot.getKey().equals(m.getId())) {
                        matched = false;
                    } else {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    addNewMessage(arrayList, messages, adminChatAdapter, saverModel);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


    }

    private void addNewMessage(ArrayList<SaverModel> arrayList, List<MessageModel> messages, AdminChatAdapter adminChatAdapter, SaverModel saverModel) {
        arrayList.add(saverModel);
        MessageModel mm = new MessageModel();
        mm.setId(saverModel.getMap().get("id"));
        mm.setEmail(saverModel.getMap().get("email"));
        mm.setIs_admin(saverModel.getMap().get("is_admin"));
        mm.setMessage(saverModel.getMap().get("message"));
        mm.setView_type(saverModel.getMap().get("view_type"));
        messages.add(mm);
        adminChatAdapter.notifyDataSetChanged();
        messageRV.scrollToPosition(adminChatAdapter.getItemCount() - 1);
    }


}
