package com.webxert.listeningsouls;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.webxert.listeningsouls.Remote.APIClient;
import com.webxert.listeningsouls.Remote.RetrofitBuilder;
import com.webxert.listeningsouls.adapters.UserChatMessageAdapter;
import com.webxert.listeningsouls.common.Common;
import com.webxert.listeningsouls.common.Constants;
import com.webxert.listeningsouls.fragments.UserChatFragment;
import com.webxert.listeningsouls.interfaces.LogoutListener;
import com.webxert.listeningsouls.interfaces.UserIdentityListener;
import com.webxert.listeningsouls.models.ChatModel;
import com.webxert.listeningsouls.models.DataMessage;
import com.webxert.listeningsouls.models.MessageModel;
import com.webxert.listeningsouls.models.NotificationResponse;
import com.webxert.listeningsouls.models.SaverModel;
import com.webxert.listeningsouls.models.User;
import com.webxert.listeningsouls.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements LogoutListener, UserIdentityListener {


    private static final int IMAGE_PICK_CODE_USER_TO_ADMIN = 0;
    private static final int IMAGE_PICK_CODE_ADMIN_TO_USER = 1;
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
    ValueEventListener seenListener;
    DatabaseReference seenReference;
    ImageView attach_files;
    DatabaseReference markSeenRef;
    public static String USER_ID_FROM_FRAGMENT = "";
    public static Uri IMAGE_PICKED_ADDRESS = null;
    ProgressBar progressBar;
    Retrofit retrofit;
    APIClient client;
    String myId;
    private int imageCount = 0;


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
        } else if (R.id.log == item.getItemId()) {
            return false;
        } else if (R.id.assign_to == item.getItemId())
            return false;
        else if (R.id.convo_status == item.getItemId())
            return false;
        else if (R.id.login == item.getItemId()) {
            showStatusDialog();
            return false;
        } else if (R.id.block_user == item.getItemId())
            return false;

        return true;
    }

    private void showStatusDialog() {
        View view = getLayoutInflater().inflate(R.layout.convo_details, null);
        final TextView by = view.findViewById(R.id.by);
        final TextView sentHeadingTxt = view.findViewById(R.id.sent_by_dummy);
        final TextView seenTxt = view.findViewById(R.id.seen_text);
        final Button dismiss = view.findViewById(R.id.dismiss);
        by.setVisibility(View.GONE);
        sentHeadingTxt.setVisibility(View.GONE);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        DatabaseReference seenReference = FirebaseDatabase.getInstance().getReference("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.DOMAIN_NAME);
        Query query = seenReference.limitToLast(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    MessageModel model = dataSnapshot.getChildren().iterator().next().getValue(MessageModel.class);
                    if (model.getId_sender().equals(Constants.DOMAIN_NAME) && model.getId_receiver()
                            .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        //by.setText("Admin: -> " + Common.getPersonName(dataSnapshot.child("id").getValue().toString()));
                        seenTxt.setText("You have no pending messages!");
                        //seenTxt.setText(dataSnapshot.child("status").getValue().toString());
                    } else if (model.getId_sender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && model.getId_receiver()
                            .equals(Constants.DOMAIN_NAME)) {
                        seenTxt.setText(model.getStatus());
                    }

                } else
                    seenTxt.setText("No messages!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MessageStatus", databaseError.getMessage());
                dialog.dismiss();
            }
        });
    }

    private void updateChildren(DatabaseReference key, MessageModel model) {
        model.setStatus("Seen");
        key.setValue(model);
        Log.e("Updated", "Updated at " + key.getKey());
    }

    public void showMediaLayout() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ABOVE, R.id.message_text);
        user_recyclerview.setLayoutParams(params);
        TextView blockedTV = findViewById(R.id.blocked_message);
        blockedTV.setVisibility(View.GONE);
        submit_button.setVisibility(View.VISIBLE);
        message_text.setVisibility(View.VISIBLE);
        attach_files.setVisibility(View.VISIBLE);
    }

    public void hideMediaLayout() {

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        params.setMargins(0, 0, 0, 35);
        user_recyclerview.setLayoutParams(params);
        TextView blockedTV = findViewById(R.id.blocked_message);
        blockedTV.setVisibility(View.VISIBLE);
        submit_button.setVisibility(View.GONE);
        message_text.setVisibility(View.GONE);
        attach_files.setVisibility(View.GONE);
    }

    private void sendNotificationToAdmins(final String type) {

        DatabaseReference users = FirebaseDatabase.getInstance().getReference("Users");
        Query query = users.orderByChild("is_admin").equalTo(true);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    Map<String, String> map = new HashMap<>();
                    map.put("title", Constants.DOMAIN_NAME_CAPITAL);
                    if (type.equals("text"))
                        map.put("message", "New " + type + " message from " + Common.getPersonName(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                    else
                        map.put("message", imageCount + " new image(s) from " + Common.getPersonName(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                    DataMessage dataMessage = new DataMessage(user.getDevice_token(), map);
                    client.sendNotification(dataMessage).enqueue(new Callback<NotificationResponse>() {
                        @Override
                        public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success == 1)
                                    Log.e("Status", "Successful");
                                else Log.e("Status", "Failure");
                            } else Log.e("Code", response.code() + "");
                        }

                        @Override
                        public void onFailure(Call<NotificationResponse> call, Throwable t) {
                            Log.e("onFailure", t.getLocalizedMessage());
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //myId = getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).getString(Constants.ID, "");

        retrofit = RetrofitBuilder.getRetrofit();
        client = retrofit.create(APIClient.class);

        Log.e("Token", FirebaseInstanceId.getInstance().getToken());

        updateTokenToServer(FirebaseInstanceId.getInstance().getToken());

        reader = getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE);
        user_layout = findViewById(R.id.user_layout);
        admin_layout = findViewById(R.id.admin_layout);
        submit_button = findViewById(R.id.submit_button);
        progressBar = findViewById(R.id.progressBar);
        message_text = findViewById(R.id.message_text);
        email = reader.getString(Constants.USER_EMAIL, "null");
        attach_files = findViewById(R.id.select_media);


        layout_decider = reader.getString(Constants.AUTH_, "null");
        if (layout_decider.equals(Constants.ADMIN_AUTH)) {
            admin_layout.setVisibility(View.VISIBLE);
            user_layout.setVisibility(View.GONE);

            setFragment(admin_layout);


        } else if (layout_decider.equals(Constants.CUSTOMER_AUTH)) {

            progressBar.setVisibility(View.VISIBLE);
            user_layout.setVisibility(View.VISIBLE);
            admin_layout.setVisibility(View.GONE);
            user_recyclerview = findViewById(R.id.user_message_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setStackFromEnd(true);
            user_recyclerview.setLayoutManager(layoutManager);
            chatMessagesAdapter = new UserChatMessageAdapter(messages, this);
            user_recyclerview.setAdapter(chatMessagesAdapter);
            if (Common.checkBlockStatus(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                hideMediaLayout();
            } else {
                showMediaLayout();
//                                Toast.makeText(MainActivity.this, "You are blocked by admins!", Toast.LENGTH_SHORT).show();

            }

            attach_files.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, IMAGE_PICK_CODE_USER_TO_ADMIN);

                }
            });
            submit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if (!Common.checkBlockStatus(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        if (!TextUtils.isEmpty(message_text.getText().toString())) {
                            String message = message_text.getText().toString();
                            message_text.setText("");

                            FirebaseDatabase.getInstance().getReference("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(Constants.DOMAIN_NAME).push().
                                    setValue(new MessageModel(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "0", message, "0", FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                            simpleDateFormat.format(Calendar.getInstance().getTime()), "text", FirebaseAuth.getInstance().getCurrentUser().getUid(), Constants.DOMAIN_NAME, "Not Seen", "none")).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    message_text.requestFocus();
                                    //sendNotificationToAdmins("text");
                                    ChatModel model = new ChatModel();
                                    model.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    model.setSeen(false);
                                    Date date = Calendar.getInstance().getTime();
                                    Log.e("date", date.toString());
                                    model.setDate(date);
                                    model.setTimestamp(-1 * new Date().getTime());
                                    model.setAssignedTo(Paper.book().read("assign_id", "None"));
                                    model.setWith(getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).getString(Constants.USER_EMAIL, "null"));
                                    FirebaseDatabase.getInstance().getReference("chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(model);
                                    displayMessages();
                                    sendNotificationToAdmins("text");

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(MainActivity.class.getSimpleName(), e.getMessage());
                                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });


                        }

                    } else {
                        Log.e("Here", "Why here");
                        hideMediaLayout();
                    }
                }
            });

            displayMessages();


        } else {
            FrameLayout placeholder = findViewById(R.id.placeholder_layout);
            placeholder.setVisibility(View.VISIBLE);
        }


    }

    private void updateTokenToServer(String token) {
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("device_token").setValue(token);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
        Log.e("RequestCode", requestCode + "");
        if (requestCode == IMAGE_PICK_CODE_USER_TO_ADMIN && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                sendMultiImages(data.getClipData());
            } else if (data.getData() != null) {

                final Uri uri = data.getData();
                final ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("Sending Media");
                dialog.setMessage("Sending 1 of 1 image please wait");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                sendMediaAsUser(uri, dialog);
            }


        } else if (requestCode == IMAGE_PICK_CODE_ADMIN_TO_USER && resultCode == RESULT_OK && data != null) {

            IMAGE_PICKED_ADDRESS = data.getData();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("sending.as.admin"));
        }
    }

    private void sendMultiImages(final ClipData clipData) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Sending confirmation");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                dialogInterface.dismiss();
                final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                dialog.setTitle("Sending Media");
                // dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.DOMAIN_NAME);
                int totalItems = clipData.getItemCount();
                String message = "";
                dialog.setCanceledOnTouchOutside(false);


                sendNotificationToAdmins("image");

                imageCount = totalItems;
                //dialog.setMax(totalItems);
                for (int i = 0; i < totalItems; i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    message = "Sending " + (i + 1) + " of " + totalItems + " images please wait";
                    dialog.setMessage(message);
                    dialog.show();
                    String imageName = messageRef.push().getKey() + ".jpg";
                    final StorageReference imagesRef = FirebaseStorage.getInstance().getReference("images").child(imageName + ".jpg");
                    UploadTask uploadTask = imagesRef.putFile(uri);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful())
                                throw task.getException();
                            return imagesRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {

                                FirebaseDatabase.getInstance().getReference("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child(Constants.DOMAIN_NAME).push().
                                        setValue(new MessageModel(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "0", "", "0", FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                                simpleDateFormat.format(Calendar.getInstance().getTime()), "image", FirebaseAuth.getInstance().getCurrentUser().getUid(), Constants.DOMAIN_NAME, "Not Seen", task.getResult().toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        message_text.requestFocus();

                                        ChatModel model = new ChatModel();
                                        model.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        model.setSeen(false);
                                        Date date = Calendar.getInstance().getTime();
                                        Log.e("date", date.toString());
                                        model.setDate(date);
                                        model.setTimestamp(-1 * new Date().getTime());
                                        model.setAssignedTo(Paper.book().read("assign_id", "None"));
                                        model.setWith(getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).getString(Constants.USER_EMAIL, "null"));
                                        FirebaseDatabase.getInstance().getReference("chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(model);
                                        displayMessages();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(MainActivity.class.getSimpleName(), e.getMessage());
                                        Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                            } else
                                Log.e("DownloadUrlException", task.getException().getMessage());

                        }
                    });


                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
        //dialog.dismiss();
    }


    private void sendMediaAsUser(final Uri uri, final ProgressDialog dialog) {


        imageCount = 1;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Sending confirmation");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.DOMAIN_NAME);
                String imageName = messageRef.push().getKey();
                final StorageReference imagesRef = FirebaseStorage.getInstance().getReference("images").child(imageName + ".jpg");
                UploadTask uploadTask = imagesRef.putFile(uri);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful())
                            throw task.getException();
                        return imagesRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {

                            FirebaseDatabase.getInstance().getReference("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(Constants.DOMAIN_NAME).push().
                                    setValue(new MessageModel(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "0", "", "0", FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                            simpleDateFormat.format(Calendar.getInstance().getTime()), "image", FirebaseAuth.getInstance().getCurrentUser().getUid(), Constants.DOMAIN_NAME, "Not Seen", task.getResult().toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    message_text.requestFocus();


                                    ChatModel model = new ChatModel();
                                    model.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    model.setSeen(false);
                                    Date date = Calendar.getInstance().getTime();
                                    Log.e("date", date.toString());
                                    model.setDate(date);
                                    model.setTimestamp(-1 * new Date().getTime());
                                    model.setAssignedTo(Paper.book().read("assign_id", "None"));
                                    model.setWith(getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).getString(Constants.USER_EMAIL, "null"));
                                    FirebaseDatabase.getInstance().getReference("chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(model);
                                    displayMessages();
                                    sendNotificationToAdmins("image");

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(MainActivity.class.getSimpleName(), e.getMessage());
                                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                        } else
                            Log.e("DownloadUrlException", task.getException().getMessage());

                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();

    }

    private void setFragment(FrameLayout admin_layout) {
        UserChatFragment userChatFragment = new UserChatFragment();
        userChatFragment.setLogoutListener(this);
        userChatFragment.setUserIdentityListener(this);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.admin_layout, userChatFragment, "userschat");
        transaction.commit();
    }


    private void displayMessages() {

        // setLogReference();
        markStatusToSeen();
        final ProgressDialog dialog = Utils.getMessageProgressDialog(this);
        //dialog.show();

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //dialog.dismiss();
//                progressBar.setVisibility(View.GONE);
//                if (!messages_found)
//                    Toast.makeText(MainActivity.this, "No messages found!", Toast.LENGTH_SHORT).show();
//            }
//        }, 5000);

        DatabaseReference message_ref = FirebaseDatabase.getInstance().getReference("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.DOMAIN_NAME);

        DatabaseReference mr = FirebaseDatabase.getInstance().getReference("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.DOMAIN_NAME);
        mr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    progressBar.setVisibility(View.GONE);
                    // Toast.makeText(MainActivity.this, "No messages found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        message_ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                // dialog.dismiss();
                progressBar.setVisibility(View.GONE);
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
        mm.setId_sender(saverModel.getMap().get("id_sender"));
        mm.setId_receiver(saverModel.getMap().get("id_receiver"));
        mm.setStatus(saverModel.getMap().get("status"));
        mm.setImage_url(saverModel.getMap().get("image_url"));
        messages.add(mm);
        chatMessagesAdapter.notifyDataSetChanged();
        user_recyclerview.scrollToPosition(chatMessagesAdapter.getItemCount() - 1);
    }

    ValueEventListener seenEventListener;

    private void markStatusToSeen() {
//        seenReference = FirebaseDatabase.getInstance().getReference("chats").child(id);
//        seenReference.child("seen").setValue(true);
        markSeenRef = FirebaseDatabase.getInstance().getReference("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.DOMAIN_NAME);
        Query query = markSeenRef.limitToLast(1);
        seenEventListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    MessageModel model = dataSnapshot.getChildren().iterator().next().getValue(MessageModel.class);
                    Log.e("DataKey", dataSnapshot.getChildren().iterator().next().getKey());
                    Log.e("SenderReceiver", model.getId_sender() + " " + model.getId_receiver());
                    Log.e("fuserid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    if (model.getId_sender().equals(Constants.DOMAIN_NAME) && model.getId_receiver().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        updateChildren(dataSnapshot.getChildren().iterator().next().getRef(), model);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (markSeenRef != null && seenEventListener != null)
            markSeenRef.removeEventListener(seenEventListener);
    }

    @Override
    public void onLogout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).edit().clear().apply();
        FirebaseAuth.getInstance().signOut();
        startActivity(intent);
    }

    @Override
    public void onRequestCodeMatched(String id) {
        USER_ID_FROM_FRAGMENT = id;
    }
}
