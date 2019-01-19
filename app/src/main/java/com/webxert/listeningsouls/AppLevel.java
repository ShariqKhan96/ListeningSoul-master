package com.webxert.listeningsouls;

import android.app.Application;

import io.paperdb.Paper;

public class AppLevel extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Paper.init(this);
    }

}
