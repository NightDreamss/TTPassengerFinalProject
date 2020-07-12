package com.nightdream.ttpassenger.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nightdream.ttpassenger.R;

import java.util.Map;
import java.util.Objects;

public class FirebaseService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = Objects.requireNonNull(remoteMessage.getNotification()).getTitle();
        String body = remoteMessage.getNotification().getBody();

        Map<String, String> extraData = remoteMessage.getData();

        String data = extraData.get("key");

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "Emergency Message").setContentTitle(title).setContentText(body).setSmallIcon(R.drawable.ic_driver);

        if (data != null) {
            Intent intent = new Intent(this, ShareRideScreen.class);
            intent.putExtra("data", data);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            notification.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            int id = (int) System.currentTimeMillis();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("Emergency Message", "message", NotificationManager.IMPORTANCE_HIGH);
                assert notificationManager != null;
                notificationManager.createNotificationChannel(channel);
            }
            assert notificationManager != null;
            notificationManager.notify(id, notification.build());

        } else {
            Intent intent = new Intent(this, NotificationScreen.class);
            intent.putExtra("data", body);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            notification.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            int id = (int) System.currentTimeMillis();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("Emergency Message", "message", NotificationManager.IMPORTANCE_HIGH);
                assert notificationManager != null;
                notificationManager.createNotificationChannel(channel);
            }
            assert notificationManager != null;
            notificationManager.notify(id, notification.build());
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        updateToken(s);
    }

    private void updateToken(String s) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(s);
        assert user != null;
        reference.child(user.getUid()).setValue(token);

    }
}
