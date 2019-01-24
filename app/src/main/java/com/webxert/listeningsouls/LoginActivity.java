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
    ProgressDialog progressDialog;
    RemoveCallBackListener removeCallBackListener;
    boolean checkPrivillageAbuse = false;


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

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Authenticating");
        progressDialog.setMessage("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        m_auth = FirebaseAuth.getInstance();
        users_ref = FirebaseDatabase.getInstance().getReference("Users");
        getAllUsers();

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
                        final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
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
                                    alertDialog.dismiss();
                                    loginSession(progressDialog, true);


                                } else {
                                    alertDialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "Secret key not matched!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                            }
                        });


                        //dialog.show();
                    } else {
                        loginSession(progressDialog, false);
                        //Log.e("Notadmin", authResult.getUser().getUid());
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Emplty Field(s)!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loginSession(final ProgressDialog dialog, final boolean is_admin) {
        dialog.show();
        //check if admin try to login as user
        if (!checkPrivillageAbuseAttack(is_admin, email.getText().toString(), password.getText().toString())) {
            checkPrivillageAbuse = false;
            m_auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    dialog.dismiss();
                    if (is_admin)
                        writer.putString(Constants.AUTH_, Constants.Authentication.ADMIN.name());
                    else
                        writer.putString(Constants.AUTH_, Constants.Authentication.CUSTOMER.name());
                    writer.putString(Constants.USER_NAME, email.getText().toString());
                    writer.putString(Constants.USER_EMAIL, email.getText().toString());
                    writer.putBoolean(Constants.LOGIN_, true);
                    writer.apply();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            checkPrivillageAbuse = false;
            dialog.dismiss();
            Toast.makeText(this, "You cant login as user being admin!!", Toast.LENGTH_LONG).show();
        }

    }

    private boolean checkPrivillageAbuseAttack(boolean is_admin, final String email, final String password) {
        if (!is_admin) {
            for (User user : Constants.userList) {
                if (user.getEmail().equals(email) && user.getPassword().equals(password) && user.isIs_admin()) {
                    checkPrivillageAbuse = true;
                    break;
                } else
                    checkPrivillageAbuse = false;
            }
//            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
//            databaseReference.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    for (DataSnapshot data : dataSnapshot.getChildren()) {
//                        User user = data.getValue(User.class);
//                        Log.e("User", user.getEmail() + " " + user.getPassword() + " " + user.isIs_admin());
//                        if (user.getEmail().equals(email) && user.getPassword().equals(password) && user.isIs_admin()) {
//                            checkPrivillageAbuse = true;
//                            Log.e("Matched", user.isIs_admin() + "");
//                            return;
//                        } else checkPrivillageAbuse = false;
//
//
//                    }
//
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//        }
        } else
            return checkPrivillageAbuse;

        Log.e("checkPrv", checkPrivillageAbuse + "");
        return checkPrivillageAbuse;
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
