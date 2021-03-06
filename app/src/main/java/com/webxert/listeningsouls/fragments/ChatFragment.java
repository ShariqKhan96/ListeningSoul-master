package com.webxert.listeningsouls.fragments;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.webxert.listeningsouls.MainActivity;
import com.webxert.listeningsouls.R;
import com.webxert.listeningsouls.RegisterActivity;
import com.webxert.listeningsouls.adapters.ChatMessagesAdapter;
import com.webxert.listeningsouls.common.Common;
import com.webxert.listeningsouls.common.Constants;
import com.webxert.listeningsouls.interfaces.LogoutListener;
import com.webxert.listeningsouls.interfaces.UserIdentityListener;
import com.webxert.listeningsouls.models.ChatModel;
import com.webxert.listeningsouls.models.MessageModel;
import com.webxert.listeningsouls.models.SaverModel;
import com.webxert.listeningsouls.models.User;
import org.w3c.dom.Text;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

import static android.content.Context.MODE_PRIVATE;

public class ChatFragment extends Fragment {


    public static Fragment mInstance = null;
    ArrayList<SaverModel> arrayList = new ArrayList<>();
    ArrayList<MessageModel> messages = new ArrayList<>();
    boolean matched = false;
    final DatabaseReference logRef = FirebaseDatabase.getInstance().getReference("Logs");
    LogoutListener logoutListener;
    DatabaseReference markSeenRef;
    View view;
    UserIdentityListener userIdentityListener;

    public ChatFragment() {

    }

    public void setLogoutListener(LogoutListener logoutListener) {
        this.logoutListener = logoutListener;
    }

    public void setUserIdentityListener(UserIdentityListener userIdentityListener) {
        this.userIdentityListener = userIdentityListener;
    }

    public RecyclerView recyclerView;

    ImageView submit_button;
    TextView blockedTV;
    EditText message_text;
    String id;
    String email;
    DatabaseReference seenReference;
    ValueEventListener seenEventListener;
    ImageView media_select;
    private BroadcastReceiver mediaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("onReceive", "onReceive");
            final ProgressDialog dialog = new ProgressDialog(getContext());
            dialog.setTitle("Sending Media");
            dialog.setMessage("Please Wait");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            sendMediaAsAdmin(MainActivity.IMAGE_PICKED_ADDRESS, dialog);
        }
    };

    ChatMessagesAdapter chatMessagesAdapter;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.admin_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.log) {
            showNoteDialog();
            return true;
        } else if (R.id.assign_to == item.getItemId()) {
            showAssignmentDialog();

        } else if (R.id.convo_status == item.getItemId())
            showConversationStatusDalog();
        else if (R.id.logout == item.getItemId())
            logout();
        else if (R.id.block_user == item.getItemId())
            showBlockDialog();

        return true;


    }

    private void showBlockDialog() {
        View view = getLayoutInflater().inflate(R.layout.block_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        Button yes = view.findViewById(R.id.yes);
        Button cancel = view.findViewById(R.id.dismiss);
        Button unblock = view.findViewById(R.id.unblock);

        if (Common.checkBlockStatus(id)) {
            unblock.setEnabled(true);
            yes.setEnabled(false);
        } else {
            yes.setEnabled(true);
            unblock.setEnabled(false);
        }


        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                blockUser(id);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        unblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unblockUser(id);
                dialog.dismiss();
            }
        });
    }

    private void unblockUser(String id) {
        FirebaseDatabase.getInstance().getReference("Users").child(id).child("blocked").setValue(false);
        showMediaLayout();
        getAllUsers();
    }

    private void showMediaLayout() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ABOVE, R.id.message_text);
        recyclerView.setLayoutParams(params);
        submit_button.setVisibility(View.VISIBLE);
        message_text.setVisibility(View.VISIBLE);
        blockedTV.setVisibility(View.GONE);
        media_select.setVisibility(View.VISIBLE);
    }

    private void blockUser(String id) {
        FirebaseDatabase.getInstance().getReference("Users").child(id).child("blocked").setValue(true);
        hideMediaLayout();
        getAllUsers();
    }


    private void getAllUsers() {

        Paper.book().delete("users");
        Constants.userList.clear();
        final DatabaseReference db_ref = FirebaseDatabase.getInstance().getReference("Users");

        db_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Constants.userList.add(data.getValue(User.class));
                }
                Paper.book().write("users", Constants.userList);
                db_ref.removeEventListener(this);
                //db_ref.removeEventListener(this); can also simply do like this
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "" + databaseError.getMessage(), Toast.LENGTH_LONG).show();

            }
        });


    }

    private void hideMediaLayout() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        params.setMargins(0, 0, 0, 35);
        recyclerView.setLayoutParams(params);
        submit_button.setVisibility(View.GONE);
        message_text.setVisibility(View.GONE);
        blockedTV.setVisibility(View.VISIBLE);
        media_select.setVisibility(View.GONE);
    }

    private void logout() {
        logoutListener.onLogout();
    }

    private void showConversationStatusDalog() {
        View view = getLayoutInflater().inflate(R.layout.convo_details, null);
        final TextView by = view.findViewById(R.id.by);
        final TextView seenTxt = view.findViewById(R.id.seen_text);
        final Button dismiss = view.findViewById(R.id.dismiss);
        final TextView convo_status = view.findViewById(
                R.id.convo_status
        );
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        DatabaseReference seenReference = FirebaseDatabase.getInstance().getReference("Messages").child(id).child(Constants.DOMAIN_NAME);
        Query query = seenReference.limitToLast(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("Datasnapshot", dataSnapshot.getValue().toString());
                for (DataSnapshot data : dataSnapshot.getChildren()
                        ) {
                    MessageModel model = data.getValue(MessageModel.class);

                    Log.e("Model", model.getId());
                    if (dataSnapshot.getChildrenCount() > 0) {

                        if (model.getId_sender().equals(Constants.DOMAIN_NAME) && model.getId_receiver()
                                .equals(id)) {
                            by.setText(Common.getPersonName(model.getId()));
                            seenTxt.setText(model.getStatus());

                        } else if (model.getId_sender().equals(id) && model.getId_receiver()
                                .equals(Constants.DOMAIN_NAME)) {
                            //by.setText("Patient: -> " + Common.getPersonName(id));
                            by.setText("Patient");
                            //seenTxt.setText("You have no pending messages to be seen!");
                            seenTxt.setVisibility(View.GONE);
                            convo_status.setVisibility(View.GONE);
                        }

                    } else {
                        seenTxt.setText("No messages!");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MessageStatus", databaseError.getMessage());
                dialog.dismiss();
            }
        });
    }

    private void showNoteDialog() {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = getLayoutInflater().inflate(R.layout.log_dialog, null);
        final EditText edtNote = view.findViewById(R.id.edtNote);
        final TextView note = view.findViewById(R.id.note);
        final TextView by = view.findViewById(R.id.by);
        Button reset = view.findViewById(R.id.remove);
        Button update = view.findViewById(R.id.update);
        Button cancel = view.findViewById(R.id.dismiss);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        edtNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                note.setText(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        final DatabaseReference logRef = FirebaseDatabase.getInstance().getReference("Logs").child(id);

        logRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    Log.e("note", dataSnapshot.child("note").getValue().toString());
                    note.setText(dataSnapshot.child("note").getValue().toString());
                    by.setText(new StringBuilder(dataSnapshot.child("by").getValue().toString().toUpperCase()).append(" At ").append(dataSnapshot.child("time").getValue().toString()));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                note.setText("");
                edtNote.setText("");
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> map = new HashMap<>();
                map.put("note", edtNote.getText().toString());
                map.put("by", Common.getPersonName(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                map.put("time", format.format(Calendar.getInstance().getTime()));
                logRef.updateChildren(map);
                dialog.dismiss();

            }
        });

    }

    String[] usersName;
    String[] usersIds;

    private void showAssignmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Assign To:");

        usersName = new String[Constants.adminList.size()];
        usersIds = new String[Constants.adminList.size()];

        for (int i = 0; i < Constants.adminList.size(); i++) {
//            String is_admin = Constants.adminList.get(i).isIs_admin() + "";
//            Log.e("admin", is_admin);
//            if (is_admin.toString().equals("true")) {
            usersName[i] = Constants.adminList.get(i).getName();
            usersIds[i] = Constants.adminList.get(i).getId();


        }


        builder.setItems(usersName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Toast.makeText(getContext(), "Position: " + which + " Value: " + usersName[which], Toast.LENGTH_LONG).show();
                String assignedUserId = usersIds[which];
                updateAssignedId(assignedUserId);
                Paper.book().write("assign_id", assignedUserId);
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateAssignedId(String assignedUserId) {
        FirebaseDatabase.getInstance().getReference("chats").child(id).child("assignedTo").setValue(assignedUserId);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mediaReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.user_message_list);
        submit_button = view.findViewById(R.id.submit_button);
        blockedTV = view.findViewById(R.id.blocked_message);
        media_select = view.findViewById(R.id.select_media);
        message_text = view.findViewById(R.id.message_text);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mediaReceiver, new IntentFilter("sending.as.admin"));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        chatMessagesAdapter = new ChatMessagesAdapter(messages, getContext());
        recyclerView.setAdapter(chatMessagesAdapter);
        id = getArguments().getString("id");
        email = getArguments().getString("email");
        Constants.adminList.clear();
        Constants.adminList = Paper.book().read("admins");


        // displayMessages();
        if (Common.checkBlockStatus(id))
            hideMediaLayout();

        media_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                userIdentityListener.onRequestCodeMatched(id);
                getActivity().startActivityForResult(intent, 1);
            }
        });

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(message_text.getText().toString())) {
                    String message = message_text.getText().toString();
                    message_text.setText("");
                    if (!Common.checkBlockStatus(id)) {
                        FirebaseDatabase.getInstance().getReference("Messages").child(id)
                                .child(Constants.DOMAIN_NAME).push().setValue
                                (new MessageModel(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "1",
                                        message, "1", FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                        simpleDateFormat.format(Calendar.getInstance().getTime()), "text", Constants.DOMAIN_NAME, id, "Not Seen", "none")).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //message_text.setText("");
                                message_text.requestFocus();

                                displayMessages();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(MainActivity.class.getSimpleName(), e.getMessage());

                            }
                        });
                    } else {
                        hideMediaLayout();
                    }

                }
            }
        });

        displayMessages();

        return view;

    }

    public static Fragment getmInstance() {
        if (mInstance == null)
            return mInstance = new ChatFragment();
        else return mInstance;
    }

    private void displayMessages() {

        markStatusToSeen();
        DatabaseReference message_ref = FirebaseDatabase.getInstance().getReference("Messages").child(id).child(Constants.DOMAIN_NAME);

        message_ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
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

    private void markStatusToSeen() {
        seenReference = FirebaseDatabase.getInstance().getReference("chats").child(id);
        seenReference.child("seen").setValue(true);
        markSeenRef = FirebaseDatabase.getInstance().getReference("Messages").child(id).child(Constants.DOMAIN_NAME);
        Query query = markSeenRef.limitToLast(1);
        seenEventListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() > 0) {
                    MessageModel model = dataSnapshot.getChildren().iterator().next().getValue(MessageModel.class);

                    if (model.getId_sender().equals(id) && model.getId_receiver().equals(Constants.DOMAIN_NAME))
                        updateChildren(dataSnapshot.getChildren().iterator().next().getRef(), model);

                    // markSeenRef.child(dataSnapshot.getKey()).child("status").setValue("Seen");
//                    dataSnapshot.getRef().setValue(model);

                }

//                    if (model.getId_sender().equals(id) && model.getId_receiver()
//                            .equals(Constants.DOMAIN_NAME)) {
//                        dataSnapshot.getRef().child("status").setValue("Seen");
//                    }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateChildren(DatabaseReference key, MessageModel model) {
        model.setStatus("Seen");
        key.setValue(model);
        Log.e("Updated", "Updated at: " + key.getKey());
    }

    private void addNewMessage(ArrayList<SaverModel> arrayList, ArrayList<MessageModel> messages, ChatMessagesAdapter chatMessagesAdapter, SaverModel saverModel) {
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
        recyclerView.scrollToPosition(chatMessagesAdapter.getItemCount() - 1);
    }

    @Override
    public void onPause() {
        super.onPause();
        markSeenRef.removeEventListener(seenEventListener);
    }

    private void sendMediaAsAdmin(Uri uri, final ProgressDialog dialog) {
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("Messages").child(id).child(Constants.DOMAIN_NAME);
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
                            setValue(new MessageModel(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "0", "", "1", FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    simpleDateFormat.format(Calendar.getInstance().getTime()), "image", Constants.DOMAIN_NAME, id, "Not Seen", task.getResult().toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            message_text.requestFocus();
//                            ChatModel model = new ChatModel();
//                            model.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                            model.setSeen(false);
//                            Date date = Calendar.getInstance().getTime();
//                            Log.e("date", date.toString());
//                            model.setDate(date);
//                            model.setTimestamp(-1 * new Date().getTime());
//                            model.setAssignedTo(Paper.book().read("assign_id", "None"));
//                            model.setWith(getContext().getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).getString(Constants.USER_EMAIL, "null"));
//                            FirebaseDatabase.getInstance().getReference("chats").child(id)
//                                    .setValue(model);
                            displayMessages();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(MainActivity.class.getSimpleName(), e.getMessage());
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                } else
                    Log.e("DownloadUrlException", task.getException().getMessage());

            }
        });
    }
}
