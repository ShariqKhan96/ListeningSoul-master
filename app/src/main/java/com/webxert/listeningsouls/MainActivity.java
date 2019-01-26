package com.webxert.listeningsouls;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.webxert.listeningsouls.adapters.UserChatMessageAdapter;
import com.webxert.listeningsouls.common.Constants;
import com.webxert.listeningsouls.fragments.UserChatFragment;
import com.webxert.listeningsouls.models.ChatModel;
import com.webxert.listeningsouls.models.MessageModel;
import com.webxert.listeningsouls.models.SaverModel;
import com.webxert.listeningsouls.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    ArrayList<SaverModel> arrayList = new ArrayList<>();
    ArrayList<MessageModel> messages = new ArrayList<>();
    boolean messages_found = false;
    RelativeLayout user_layout;
    FrameLayout admin_layout;
    SharedPreferences reader;
    ImageView submit_button;
    String email;
    EditText message_text;
    String layout_decider = "";
    RecyclerView user_recyclerview;
    boolean matched = false;
    boolean is_have_messages = false;
    UserChatMessageAdapter chatMessagesAdapter;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).edit().clear().apply();
            FirebaseAuth.getInstance().signOut();
            startActivity(intent);
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        reader = getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE);
        user_layout = findViewById(R.id.user_layout);
        admin_layout = findViewById(R.id.admin_layout);
        submit_button = findViewById(R.id.submit_button);
        message_text = findViewById(R.id.message_text);
        email = reader.getString(Constants.USER_EMAIL, "null");


        layout_decider = reader.getString(Constants.AUTH_, "null");
        if (layout_decider.equals(Constants.ADMIN_AUTH)) {
            admin_layout.setVisibility(View.VISIBLE);
            user_layout.setVisibility(View.GONE);

            setFragment(admin_layout);


        } else if (layout_decider.equals(Constants.CUSTOMER_AUTH)) {

            user_layout.setVisibility(View.VISIBLE);
            admin_layout.setVisibility(View.GONE);
            user_recyclerview = findViewById(R.id.user_message_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setStackFromEnd(true);
            user_recyclerview.setLayoutManager(layoutManager);
            chatMessagesAdapter = new UserChatMessageAdapter(messages, this);
            user_recyclerview.setAdapter(chatMessagesAdapter);


            submit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!TextUtils.isEmpty(message_text.getText().toString())) {
                        String message = message_text.getText().toString();
                        message_text.setText("");
                        FirebaseDatabase.getInstance().getReference("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(Constants.DOMAIN_NAME).push().setValue(new MessageModel(email, "0", message, "0", FirebaseAuth.getInstance().getCurrentUser().getUid(), simpleDateFormat.format(Calendar.getInstance().getTime()), "text")).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                message_text.requestFocus();

                                ChatModel model = new ChatModel();
                                model.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                model.setSeen(false);

                                Date date = Calendar.getInstance().getTime();
                                Log.e("date",date.toString());
                                model.setDate(date);
                                model.setTimestamp(-1*new Date().getTime());
                                model.setWith(getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).getString(Constants.USER_EMAIL, "null"));
                                FirebaseDatabase.getInstance().getReference("chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(model);
                                displayMessages();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(MainActivity.class.getSimpleName(), e.getMessage());

                            }
                        });
                    }
                }
            });

            displayMessages();


        } else {
            FrameLayout placeholder = findViewById(R.id.placeholder_layout);
            placeholder.setVisibility(View.VISIBLE);
        }


    }

    private void setFragment(FrameLayout admin_layout) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.admin_layout, new UserChatFragment(), "userschat");
        transaction.commit();
    }


    private void displayMessages() {

        final ProgressDialog dialog = Utils.getMessageProgressDialog(this);
        dialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                if (!messages_found)
                    Toast.makeText(MainActivity.this, "No messages found!", Toast.LENGTH_SHORT).show();
            }
        }, 5000);

        DatabaseReference message_ref = FirebaseDatabase.getInstance().getReference("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.DOMAIN_NAME);

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
                    addNewMessage(arrayList, messages, chatMessagesAdapter, saverModel);
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
                    addNewMessage(arrayList, messages, chatMessagesAdapter, saverModel);
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

    private void addNewMessage(ArrayList<SaverModel> arrayList, ArrayList<MessageModel> messages, UserChatMessageAdapter chatMessagesAdapter, SaverModel saverModel) {
        arrayList.add(saverModel);
        MessageModel mm = new MessageModel();
        mm.setId(saverModel.getMap().get("id"));
        mm.setEmail(saverModel.getMap().get("email"));
        mm.setIs_admin(saverModel.getMap().get("is_admin"));
        mm.setMessage(saverModel.getMap().get("message"));
        mm.setView_type(saverModel.getMap().get("view_type"));
        mm.setMessage_type(saverModel.getMap().get("message_type"));
        mm.setSent_time(saverModel.getMap().get("sent_time"));
        messages.add(mm);
        chatMessagesAdapter.notifyDataSetChanged();
        user_recyclerview.scrollToPosition(chatMessagesAdapter.getItemCount() - 1);
    }

}
