package com.launcher.mummu.cabclient.pushservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.launcher.mummu.cabclient.R;
import com.launcher.mummu.cabclient.activities.MainActivity;
import com.launcher.mummu.cabclient.models.MessageModel;
import com.launcher.mummu.cabclient.utils.UIUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by muhammed on 2/14/2017.
 */

public class FirBaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String body = remoteMessage.getData().get("data");

        if (body != null && body.length() > 0) {
            MessageModel messageModel = getParsedResult(body);
            sendTo(messageModel);
        } else {
            MessageModel model = new MessageModel();
            model.setMessage(remoteMessage.getNotification().getBody());
            model.setButtonText("DISMISS");
            sendTo(model);
        }
        super.onMessageReceived(remoteMessage);
    }

    private void sendTo(MessageModel messageModel) {
        if (UIUtil.isAppIsInBackground(this)) {
            sendNotification(messageModel.getMessage(), messageModel);
        } else {
            EventBus.getDefault().post(messageModel);
        }
    }

    private MessageModel getParsedResult(String body) {
        MessageModel model = new MessageModel();
        try {
            JSONObject jsonObject = new JSONObject(body);
            String title = jsonObject.getString("title");
            String message = jsonObject.getString("message");
            String imgurl = jsonObject.getString("imgurl");
            String buttontext = jsonObject.getString("buttontext");

            model.setTitle(title);
            model.setMessage(message);
            model.setImageUrl(imgurl);
            model.setButtonText(buttontext);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return model;
    }

    private void sendNotification(String messageBody, MessageModel messageModel) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        intent.putExtra("DATA", messageModel);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
