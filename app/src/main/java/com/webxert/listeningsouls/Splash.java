package com.webxert.listeningsouls;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.webxert.listeningsouls.common.Constants;

public class Splash extends AppCompatActivity {

    SharedPreferences reader;


    @Override
    protected void onStart() {
        super.onStart();
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
        }, Constants.SPLASH_TIME);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        reader = getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE);


    }
}
