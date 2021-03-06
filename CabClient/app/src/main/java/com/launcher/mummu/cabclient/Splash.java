package com.launcher.mummu.cabclient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.launcher.mummu.cabclient.activities.Container;
import com.launcher.mummu.cabclient.activities.LoginActivity;
import com.launcher.mummu.cabclient.activities.MainActivity;
import com.launcher.mummu.cabclient.storage.CabStorageUtil;

public class Splash extends Container {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null) {
            Log.d("TOKEN_NEW", "onTokenRefresh: " + token);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (CabStorageUtil.isLogged(Splash.this, CabStorageUtil.IS_LOGGED)) {
                    startActivity(new Intent(Splash.this, MainActivity.class));
                } else {
                    startActivity(new Intent(Splash.this, LoginActivity.class));
                }
                finish();
            }
        }, 1000 * 3);
    }
}
