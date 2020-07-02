package com.nightdream.ttpassenger;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class QrCodeHandler extends AppCompatActivity {

    private ImageView qrCode;
    private String encrypted_qrCode, keyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Make to run your application only in portrait mode
        setContentView(R.layout.activity_qr_code_handler);

        variables();
        qrCodeBuilder();
    }

    private void qrCodeBuilder() {
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        keyId = bundle.getString("keyId");

        assert keyId != null;
        if (!keyId.isEmpty()) {
            String encrypted = "";
            try {
                encrypted = AESUtils.encrypt(keyId);
                encrypted_qrCode = encrypted;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        new QrCodeDownloader(qrCode).execute("https://chart.googleapis.com/chart?cht=qr&chs=500x500&chl=" + encrypted_qrCode + "&chld=H|0");
    }

    private void variables() {
        qrCode = findViewById(R.id.qr_Code);
    }
}