package com.launcher.mummu.cabclient.utils;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

/**
 * Created by muhammed on 3/6/2017.
 */

public class NetworkManager {

    private static NetworkManager networkManager;
    private OnApiResult onApiResult;

    private NetworkManager() {
    }

    public static NetworkManager getInstance() {
        return networkManager == null ? networkManager = new NetworkManager() : networkManager;
    }

    public NetworkManager setOnApiReslutCallback(OnApiResult onApiReslut) {
        this.onApiResult = onApiReslut;
        return networkManager;
    }

    public NetworkManager call(String url, Context context) {
        Ion.with(context)
                .load(url)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (onApiResult != null) {
                            onApiResult.onResult(e, result);
                        }
                    }
                });
        return networkManager;
    }

    public interface OnApiResult {
        void onResult(Exception e, JsonObject result);
    }
}
