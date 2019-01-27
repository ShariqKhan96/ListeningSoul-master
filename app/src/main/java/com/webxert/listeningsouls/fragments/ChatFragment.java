package com.webxert.listeningsouls.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.webxert.listeningsouls.MainActivity;
import com.webxert.listeningsouls.R;
import com.webxert.listeningsouls.adapters.ChatMessagesAdapter;
import com.webxert.listeningsouls.common.Common;
import com.webxert.listeningsouls.common.Constants;
import com.webxert.listeningsouls.models.ChatModel;
import com.webxert.listeningsouls.models.MessageModel;
import com.webxert.listeningsouls.models.SaverModel;

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


    public ChatFragment() {

    }

    public RecyclerView recyclerView;

    ImageView submit_button;
    EditText message_text;
    String id;
    String email;
    DatabaseReference seenReference;

    ChatMessagesAdapter chatMessagesAdapter;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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

        }
        return true;


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
                logRef.child(id).updateChildren(map);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.user_message_list);
        submit_button = view.findViewById(R.id.submit_button);
        message_text = view.findViewById(R.id.message_text);
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

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(message_text.getText().toString())) {
                    String message = message_text.getText().toString();
                    message_text.setText("");
                    FirebaseDatabase.getInstance().getReference("Messages").child(id)
                            .child(Constants.DOMAIN_NAME).push().setValue
                            (new MessageModel(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "1", message, "1", FirebaseAuth.getInstance().getCurrentUser().getUid(), simpleDateFormat.format(Calendar.getInstance().getTime()), "text")).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //message_text.setText("");
                            message_text.requestFocus();
//                            ChatModel model = new ChatModel();
//                            model.setId(id);
//                            model.setSeen(false);
//                            Date date = Calendar.getInstance().getTime();
//                            Log.e("date", date.toString());
//                            model.setDate(date);
//                            model.setTimestamp(-1 * new Date().getTime());
//                            model.setWith(email);
//                            FirebaseDatabase.getInstance().getReference("chats").child(id)
//                                    .setValue(model);
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
        messages.add(mm);
        chatMessagesAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(chatMessagesAdapter.getItemCount() - 1);
    }


}
