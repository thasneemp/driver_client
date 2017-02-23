package com.launcher.mummu.cabclient.pushservice;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by muhammed on 2/14/2017.
 */

public class FireBaseInstanceCreated extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("TOKEN_NEW", "onTokenRefresh: " + token);
        super.onTokenRefresh();
    }
}
