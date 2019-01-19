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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.webxert.listeningsouls.common.Constants;
import com.webxert.listeningsouls.models.User;
import com.webxert.listeningsouls.utils.Utils;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences.Editor writer;
    EditText email, password, name;
    Button signup;
    FirebaseAuth m_auth;
    DatabaseReference db_ref;
    CheckBox is_admin;
    boolean is_adm = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        is_admin = findViewById(R.id.is_admin);


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
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                        builder.setTitle("Authentication!");
                        builder.setMessage("Enter secret key if you are a team member");
                        final EditText editText = new EditText(RegisterActivity.this);
                        builder.setView(editText);

                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                if (editText.getText().toString().equals(Constants.DOMAIN_NAME)) {
                                    writer.putString(Constants.AUTH_, Constants.Authentication.ADMIN.name());
                                    writer.putBoolean(Constants.LOGIN_, true);
                                    writer.putString(Constants.USER_EMAIL, email.getText().toString());
                                    writer.putString(Constants.USER_NAME, name.getText().toString());
                                    writer.apply();
                                    progressDialog.show();
                                    m_auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            progressDialog.dismiss();
                                            User user = new User(authResult.getUser().getUid(), email.getText().toString(), name.getText().toString(), password.getText().toString(), "123", true);
                                            db_ref.child(authResult.getUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Log.e(RegisterActivity.class.getSimpleName(), e.getMessage());
                                                }
                                            });

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Log.e(RegisterActivity.class.getSimpleName(), e.getMessage());

                                        }
                                    });


                                } else {
                                    dialog.dismiss();
                                    progressDialog.dismiss();
                                    resetFields();
                                    Toast.makeText(RegisterActivity.this, "Your secret key is wrong!!!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                progressDialog.dismiss();
                            }
                        });

                        builder.show();

                    } else {

                        progressDialog.show();
                        m_auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                progressDialog.dismiss();
                                writer.putString(Constants.AUTH_, Constants.Authentication.CUSTOMER.name());
                                writer.putBoolean(Constants.LOGIN_, true);
                                writer.putString(Constants.USER_EMAIL, email.getText().toString());
                                writer.putString(Constants.USER_NAME, name.getText().toString());
                                writer.apply();
                                User user = new User(authResult.getUser().getUid(), email.getText().toString(), name.getText().toString(), password.getText().toString(), "123", is_adm);
                                db_ref.child(authResult.getUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Log.e(RegisterActivity.class.getSimpleName(), e.getMessage());
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Log.e(RegisterActivity.class.getSimpleName(), e.getMessage());

                            }
                        });
                    }


                } else
                    Toast.makeText(RegisterActivity.this, "Fill all fields first!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resetFields() {
        name.setText("");
        password.setText("");
        email.setText("");
        is_admin.setChecked(false);
    }
}
