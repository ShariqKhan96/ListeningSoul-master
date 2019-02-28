package com.webxert.listeningsouls;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.webxert.listeningsouls.Remote.APIClient;
import com.webxert.listeningsouls.Remote.RetrofitBuilder;
import com.webxert.listeningsouls.adapters.AdminChatAdapter;
import com.webxert.listeningsouls.common.Common;
import com.webxert.listeningsouls.common.Constants;
import com.webxert.listeningsouls.interfaces.LogoutListener;
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
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AdminChatActivity extends AppCompatActivity {

    private static final int ADMIN_MEDIA_PICK_CODE = 123;
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
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    LogoutListener logoutListener;
    APIClient client;
    Retrofit retrofit;
    ImageView media_select;
    ProgressBar progressBar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.admin_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.logout == item.getItemId()) {
            logout();
            return true;
        }
        return true;
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).edit().clear().apply();
        FirebaseAuth.getInstance().signOut();
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat);
        message_text = findViewById(R.id.message_text);
        messageRV = findViewById(R.id.user_message_list);
        submit_button = findViewById(R.id.submit_button);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageRV.setLayoutManager(linearLayoutManager);
        adminChatAdapter = new AdminChatAdapter(messages, this);
        messageRV.setAdapter(adminChatAdapter);
        reader = getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE);
        email = reader.getString(Constants.USER_EMAIL, "null");
        media_select = findViewById(R.id.select_media);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        retrofit = RetrofitBuilder.getRetrofit();
        client = retrofit.create(APIClient.class);


        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(message_text.getText().toString())) {
                    String message = message_text.getText().toString();
                    message_text.setText("");
                    FirebaseDatabase.getInstance().getReference("AdminMessages").push().
                            setValue(new MessageModel(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "1", message, "1", FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    simpleDateFormat.format(Calendar.getInstance().getTime()), "text", Constants.DOMAIN_NAME, Constants.DOMAIN_NAME, "Not Seen", "")).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            message_text.requestFocus();
                            displayMessages();
                            //  sendNotificationToAdmins("text");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(MainActivity.class.getSimpleName(), e.getMessage());
                            Toast.makeText(AdminChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        });

        media_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, ADMIN_MEDIA_PICK_CODE);
            }
        });


        displayMessages();
    }

    private void sendNotificationToAdmins(final String type) {

        DatabaseReference users = FirebaseDatabase.getInstance().getReference("Users");
        Query query = users.orderByChild("is_admin").equalTo(true);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (!user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                        Notification notification = new Notification(Constants.DOMAIN_NAME_CAPITAL, "New " + type + " message from " + Common.getPersonName(FirebaseAuth.getInstance().getCurrentUser().getUid()));
//                        NotificationSender sender = new NotificationSender(user.getDevice_token(), notification);
                        Map<String, String> map = new HashMap<>();
                        map.put("title", Constants.DOMAIN_NAME_CAPITAL);
                        map.put("message", "New " + type + " message from " + Common.getPersonName(FirebaseAuth.getInstance().getCurrentUser().getUid()));
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DatabaseError", databaseError.getMessage());
            }
        });
    }

    private void sendMediaAsAdmin(final Uri uri, final ProgressDialog dialog) {

        AlertDialog.Builder builder = new AlertDialog.Builder(AdminChatActivity.this);
        builder.setTitle("Sending confirmation");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                DatabaseReference message_ref = FirebaseDatabase.getInstance().getReference("AdminMessages");
                String imageName = message_ref.push().getKey();
                final StorageReference imagesRef = FirebaseStorage.getInstance().getReference("images").child(imageName + ".jpg");
                UploadTask uploadTask = imagesRef.putFile(uri);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        Log.e("UploadTaskStatus", task.getResult().toString());
                        if (!task.isSuccessful())
                            throw task.getException();
                        return imagesRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {

                            FirebaseDatabase.getInstance().getReference("AdminMessages").push().
                                    setValue(new MessageModel(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "0", "", "1", FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                            simpleDateFormat.format(Calendar.getInstance().getTime()), "image", Constants.DOMAIN_NAME, Constants.DOMAIN_NAME, "Not Seen", task.getResult().toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    message_text.requestFocus();
                                    //sendNotificationToAdmins("image");
                                    displayMessages();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(MainActivity.class.getSimpleName(), e.getMessage());
                                    Toast.makeText(AdminChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

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

    private void displayMessages() {

        final ProgressDialog dialog = Utils.getMessageProgressDialog(this);
        //dialog.show();

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                dialog.dismiss();
//                if (!messages_found) {
//                    Toast.makeText(AdminChatActivity.this, "No messages found!", Toast.LENGTH_SHORT).show();
//                    progressBar.setVisibility(View.GONE);
//                }
//
//            }
//        }, 5000);

        DatabaseReference mr = FirebaseDatabase.getInstance().getReference("AdminMessages");
        mr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0)
                    progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference message_ref = FirebaseDatabase.getInstance().getReference("AdminMessages");

        message_ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //dialog.dismiss();
                messages_found = true;
                progressBar.setVisibility(View.GONE);
                //Toast.makeText(MainActivity.this, ""+dataSnapshot.getChildrenCount(), Toast.LENGTH_SHORT).show();
                Map<String, String> map = (Map) dataSnapshot.getValue();

                Log.e("time", map.get("sent_time").toString());
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
        mm.setMessage_type(saverModel.getMap().get("message_type"));
        mm.setSent_time(saverModel.getMap().get("sent_time"));
        mm.setId_sender(saverModel.getMap().get("id_sender"));
        mm.setId_receiver(saverModel.getMap().get("id_receiver"));
        mm.setStatus(saverModel.getMap().get("status"));
        messages.add(mm);
        adminChatAdapter.notifyDataSetChanged();
        messageRV.scrollToPosition(adminChatAdapter.getItemCount() - 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == ADMIN_MEDIA_PICK_CODE) {
            if (data.getClipData() != null) {
                sendMultiImages(data.getClipData());
            } else if (data.getData() != null) {
                final Uri uri = data.getData();
                final ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("Sending Media");
                dialog.setMessage("Please Wait");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                sendMediaAsAdmin(uri, dialog);
            }

        }
    }

    private void sendMultiImages(final ClipData clipData) {

        AlertDialog.Builder builder = new AlertDialog.Builder(AdminChatActivity.this);
        builder.setTitle("Sending confirmation");
        builder.setMessage("Are you sure?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                dialogInterface.dismiss();
                final ProgressDialog dialog = new ProgressDialog(AdminChatActivity.this);
                dialog.setTitle("Please Wait");
                // dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("AdminMessages");
                int totalItems = clipData.getItemCount();
                String message = "";

                //dialog.setMax(totalItems);
                for (int i = 0; i < totalItems; i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    message = "Sending " + i + 1 + " of " + totalItems;
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

                                FirebaseDatabase.getInstance().getReference("AdminMessages").push().
                                        setValue(new MessageModel(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "0", "", "1", FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                                simpleDateFormat.format(Calendar.getInstance().getTime()), "image", Constants.DOMAIN_NAME, Constants.DOMAIN_NAME, "Not Seen", task.getResult().toString()))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                        Toast.makeText(AdminChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

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
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });
        builder.show();

        // dialog.dismiss();
    }
}
