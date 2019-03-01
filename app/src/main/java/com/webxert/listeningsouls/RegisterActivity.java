package com.webxert.listeningsouls;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.webxert.listeningsouls.common.Constants;
import com.webxert.listeningsouls.dialogs.CustomDialog;
import com.webxert.listeningsouls.interfaces.RemoveCallBackListener;
import com.webxert.listeningsouls.models.User;
import com.webxert.listeningsouls.utils.Utils;

import io.paperdb.Paper;

public class RegisterActivity extends AppCompatActivity implements RemoveCallBackListener {

    SharedPreferences.Editor writer;
    EditText email, password, name;
    Button signup;
    FirebaseAuth m_auth;
    DatabaseReference db_ref;
    CheckBox is_admin;
    boolean is_adm = false;
    ValueEventListener valueEventListener;
    RemoveCallBackListener removeCallBackListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        is_admin = findViewById(R.id.is_admin);

        removeCallBackListener = this;

        m_auth = FirebaseAuth.getInstance();
        db_ref = FirebaseDatabase.getInstance().getReference("Users");

        signup = findViewById(R.id.register);
        writer = getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).edit();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = Utils.getRegisterationDialog(RegisterActivity.this);
                if (!TextUtils.isEmpty(email.getText().toString()) && !TextUtils.isEmpty(password.getText().toString()) &&
                        !TextUtils.isEmpty(name.getText().toString())) {

                    if (is_admin.isChecked())
                        is_adm = true;

                    if (is_adm) {
                        final AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterActivity.this);
                        View view = getLayoutInflater().inflate(R.layout.auth_dialog, null);
                        dialog.setView(view);
                        Button yes = view.findViewById(R.id.yes);
                        Button no = view.findViewById(R.id.no);
                        final EditText edtAuth = view.findViewById(R.id.edtAuth);

                        final AlertDialog alertDialog = dialog.show();
                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (edtAuth.getText().toString().equals(Constants.DOMAIN_NAME)) {
                                    progressDialog.show();
                                    m_auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {

                                            alertDialog.dismiss();
                                            String device_token = getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).getString("device_token", "null");
                                            User user = new User(authResult.getUser().getUid(), email.getText().toString(), name.getText().toString(), password.getText().toString(), "123", true, false, device_token);
                                            writer.putString(Constants.AUTH_, Constants.Authentication.ADMIN.name());
                                            writer.putBoolean(Constants.LOGIN_, true);
                                            writer.putString(Constants.USER_EMAIL, email.getText().toString());
                                            writer.putString(Constants.USER_NAME, name.getText().toString());
                                            writer.putString(Constants.ID, authResult.getUser().getUid());
                                            writer.apply();
                                            db_ref.child(authResult.getUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    getAllUsers();
                                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    Log.e(RegisterActivity.class.getSimpleName(), e.getMessage());
                                                }
                                            });

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.e(RegisterActivity.class.getSimpleName(), e.getMessage());

                                        }
                                    });


                                } else {
                                    alertDialog.dismiss();
                                    progressDialog.dismiss();
                                    resetFields();
                                    Toast.makeText(RegisterActivity.this, "Your secret key is wrong!!!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                            }
                        });

                    } else {

                        progressDialog.show();
                        m_auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                progressDialog.dismiss();
                                writer.putString(Constants.AUTH_, Constants.Authentication.CUSTOMER.name());
                                writer.putBoolean(Constants.LOGIN_, true);
                                writer.putString(Constants.USER_EMAIL, email.getText().toString());
                                writer.putString(Constants.ID, authResult.getUser().getUid());
                                writer.putString(Constants.USER_NAME, name.getText().toString());
                                writer.apply();


                                String device_token = getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).getString("device_token", "null");
                                User user = new User(authResult.getUser().getUid(), email.getText().toString(), name.getText().toString(), password.getText().toString(), "123", false, false, device_token);
                                db_ref.child(authResult.getUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        getAllUsers();

                                        Intent intent = new Intent(RegisterActivity.this, LandActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e(RegisterActivity.class.getSimpleName(), e.getMessage());
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(RegisterActivity.class.getSimpleName(), e.getMessage());

                            }
                        });
                    }


                } else
                    Toast.makeText(RegisterActivity.this, "Fill all fields first!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getAllUsers() {

        Paper.book().delete("users");
        Paper.book().delete("admins");
        Constants.userList.clear();
        Constants.adminList.clear();

        valueEventListener = db_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Constants.userList.add(data.getValue(User.class));
                    if (data.child("is_admin").getValue().toString().equals("true"))
                        Constants.adminList.add(data.getValue(User.class));
                }
                Paper.book().write("users", Constants.userList);
                Paper.book().write("admins", Constants.adminList);
                removeCallBackListener.onRemoveCallBack();
                //db_ref.removeEventListener(this); can also simply do like this
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RegisterActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                removeCallBackListener.onRemoveCallBack();
            }
        });


    }

    private void resetFields() {
        name.setText("");
        password.setText("");
        email.setText("");
        is_admin.setChecked(false);
    }

    @Override
    public void onRemoveCallBack() {
        db_ref.removeEventListener(valueEventListener);

    }
}
