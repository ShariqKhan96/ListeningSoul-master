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
import com.webxert.listeningsouls.interfaces.RemoveCallBackListener;
import com.webxert.listeningsouls.models.User;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity implements RemoveCallBackListener {

    SharedPreferences reader;
    SharedPreferences.Editor writer;
    EditText email, password;
    CheckBox checkBox;
    Button login, signup;
    DatabaseReference users_ref;
    FirebaseAuth m_auth;
    boolean is_admin = false;
    ValueEventListener valueEventListener;
    ProgressDialog dialog;
    RemoveCallBackListener removeCallBackListener;


    @Override
    public void onRemoveCallBack() {

        users_ref.removeEventListener(valueEventListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        reader = getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE);
        writer = getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).edit();
        checkBox = findViewById(R.id.is_admin);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        removeCallBackListener = this;

        dialog = new ProgressDialog(this);
        dialog.setTitle("Authenticating");
        dialog.setMessage("Please Wait");
        dialog.setCanceledOnTouchOutside(false);
        m_auth = FirebaseAuth.getInstance();
        users_ref = FirebaseDatabase.getInstance().getReference("Users");

        login = findViewById(R.id.login);
        signup = findViewById(R.id.register);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(email.getText().toString()) && !TextUtils.isEmpty(password.getText().toString())) {
                    if (checkBox.isChecked()) {
                        // writer.putString(Constants.AUTH_, Constants.Authentication.ADMIN.name());
                        is_admin = true;
                    } else {
                        //writer.putString(Constants.AUTH_, Constants.Authentication.CUSTOMER.name());
                        is_admin = false;
                    }
                    if (is_admin) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setTitle("Authentication!");
                        builder.setMessage("Enter secret key if you are a team member");
                        final EditText editText = new EditText(LoginActivity.this);
                        builder.setView(editText);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (editText.getText().toString().equals(Constants.DOMAIN_NAME)) {
                                    dialogInterface.dismiss();
                                    loginSession(dialog, true);

                                } else {
                                    Toast.makeText(LoginActivity.this, "Secret key not matched!", Toast.LENGTH_SHORT).show();
                                    dialogInterface.dismiss();
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
                        //dialog.show();
                    } else {
                        loginSession(dialog, false);
                        //Log.e("Notadmin", authResult.getUser().getUid());
                    }
                }
            }
        });
    }

    private void loginSession(final ProgressDialog dialog, final boolean is_admin) {
        dialog.show();
        m_auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                dialog.dismiss();
                if (is_admin)
                    writer.putString(Constants.AUTH_, Constants.Authentication.ADMIN.name());
                else writer.putString(Constants.AUTH_, Constants.Authentication.CUSTOMER.name());
                writer.putString(Constants.USER_NAME, email.getText().toString());
                writer.putString(Constants.USER_EMAIL, email.getText().toString());
                writer.putBoolean(Constants.LOGIN_, true);
                writer.apply();

                getAllUsers();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAllUsers() {

        Paper.book().delete("users");
        Constants.userList.clear();

        valueEventListener = users_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()
                        ) {
                    Constants.userList.add(data.getValue(User.class));
                }
                Paper.book().write("users", Constants.userList);
                removeCallBackListener.onRemoveCallBack();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                removeCallBackListener.onRemoveCallBack();
            }
        });


    }
}
