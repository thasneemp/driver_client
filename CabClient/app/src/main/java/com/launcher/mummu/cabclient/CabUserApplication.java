package com.launcher.mummu.cabclient;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by muhammed on 2/17/2017.
 */

public class CabUserApplication extends Application {

    private static FirebaseDatabase database;

    public static FirebaseDatabase getFirebaseInstance() {
        return database = FirebaseDatabase.getInstance();
    }

    @Override
    public void onCreate() {
        FirebaseApp.initializeApp(this);
        super.onCreate();
    }
}
