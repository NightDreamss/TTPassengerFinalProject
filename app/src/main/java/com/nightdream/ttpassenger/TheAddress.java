package com.nightdream.ttpassenger;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TheAddress extends IntentService {

    private ResultReceiver receiver;

    public TheAddress() {
        super("TheAddress");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String errorAddress = "";
        if (intent != null) {
            receiver = intent.getParcelableExtra(Constants.RECEIVER);
            Location location = intent.getParcelableExtra(Constants.LOCATION_NAME);
            if (location == null) {
                return;
            }
            Geocoder geo = new Geocoder(this, Locale.getDefault());
            List<Address> addressList = null;
            try {
                addressList = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (Exception e) {
                errorAddress = e.getMessage();
            }
            if (addressList == null || addressList.isEmpty()) {
                ReceiverResults(Constants.FAIL, errorAddress);
            } else {
                Address address = addressList.get(0);
                ArrayList<String> addressFragment = new ArrayList<>();
                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++){
                    addressFragment.add(address.getAddressLine(i));
                }
                ReceiverResults(Constants.SUCCESS, TextUtils.join(Objects.requireNonNull(System.getProperty("line.separator")), addressFragment));
            }
        }
    }

    private void ReceiverResults(int resultCode, String address) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_KEY, address);
        receiver.send(resultCode, bundle);
    }
}
