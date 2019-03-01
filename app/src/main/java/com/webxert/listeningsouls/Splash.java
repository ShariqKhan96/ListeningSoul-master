package com.webxert.listeningsouls;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.webxert.listeningsouls.common.Common;
import com.webxert.listeningsouls.common.Constants;
import com.webxert.listeningsouls.models.User;

import io.paperdb.Paper;

public class Splash extends AppCompatActivity {

    SharedPreferences reader;


    @Override
    protected void onStart() {
        super.onStart();
        getAllUsers();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        reader = getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE);


    }

    private void getAllUsers() {

        Paper.book().delete("users");
        Paper.book().delete("admins");
        Constants.userList.clear();
        Constants.adminList.clear();
        final DatabaseReference db_ref = FirebaseDatabase.getInstance().getReference("Users");
        db_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Constants.userList.add(data.getValue(User.class));
                        if (data.child("is_admin").getValue().toString().equals("true"))
                            Constants.adminList.add(data.getValue(User.class));
                    }
                    Paper.book().write("users", Constants.userList);
                    Paper.book().write("admins", Constants.adminList);
                    db_ref.removeEventListener(this);
                    doSessions();

                } else doSessions();
                //db_ref.removeEventListener(this); can also simply do like this
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Splash.this, "" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                doSessions();
            }
        });


    }

    private void doSessions() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean is_login = reader.getBoolean(Constants.LOGIN_, false);
                if (is_login) {
                    String user_auth = reader.getString(Constants.AUTH_, Constants.Authentication.CUSTOMER.name());
                    Intent intent = new Intent(Splash.this, MainActivity.class);
                    if (user_auth.equals(Constants.Authentication.CUSTOMER.name())) {
                        intent.putExtra(Constants.AUTH_, Constants.Authentication.CUSTOMER.name());
                        startActivity(intent);
                        finish();
                    } else {
                        intent.putExtra(Constants.AUTH_, Constants.Authentication.ADMIN.name());
                        startActivity(intent);
                        finish();
                    }
                } else {
                    startActivity(new Intent(Splash.this, LoginActivity.class));
                    finish();
                }
            }
        }, 0);
    }
}
