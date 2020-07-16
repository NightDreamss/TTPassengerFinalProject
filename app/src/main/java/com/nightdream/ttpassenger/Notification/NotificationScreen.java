package com.nightdream.ttpassenger.Notification;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nightdream.ttpassenger.R;

public class NotificationScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_screen);

        TextView message = findViewById(R.id.Emgmessage);

        Button backbtn = findViewById(R.id.back_tomain_app_notification);

        backbtn.setOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        if (getIntent().hasExtra("data")) {
            String emgMessage = getIntent().getStringExtra("data");
            message.setText(emgMessage);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}