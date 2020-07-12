package com.nightdream.ttpassenger.Notification;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nightdream.ttpassenger.R;

public class NotificationScreen extends AppCompatActivity {

    private Button backbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_screen);

        TextView message = findViewById(R.id.Emgmessage);

        backbtn = findViewById(R.id.back_tomain_app_notification);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        if (getIntent().hasExtra("data")){
            String emgMessage = getIntent().getStringExtra("data");
            message.setText(emgMessage);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}