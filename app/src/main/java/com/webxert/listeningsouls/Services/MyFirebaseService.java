package com.webxert.listeningsouls.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.webxert.listeningsouls.R;
import com.webxert.listeningsouls.Splash;
import com.webxert.listeningsouls.common.Common;
import com.webxert.listeningsouls.common.Constants;

public class MyFirebaseService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NewToken", s);
        saveInSharedPrefs(s);
        //if (FirebaseAuth.getInstance().getCurrentUser() != null)
        // updateTokenToServer(FirebaseAuth.getInstance().getCurrentUser().getUid(), s);

    }

    private void saveInSharedPrefs(String s) {
        getSharedPreferences(Constants.SH_PREFS, MODE_PRIVATE).edit().putString("device_token", s).apply();
    }

    private void updateTokenToServer(String uid, String token) {
        FirebaseDatabase.getInstance().getReference("Users").child(uid)
                .child("device_token").setValue(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            showNotification(Common.getPersonName(remoteMessage.getFrom()), remoteMessage.getData().get("message"), remoteMessage);
        }
    }

    void showNotification(String title, String content, RemoteMessage notification) {
        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    Constants.DOMAIN_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(notification.getData().get("title"))
                // title for notification
                .setContentText(content)
                // message for notification
                .setSound(defaultUri)
                // set alarm sound for notification
                .setAutoCancel(true);
        // clear notification after click
        Intent intent = new Intent(this, Splash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(this, 123, intent, 0);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
