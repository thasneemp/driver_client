package com.launcher.mummu.cabclient.storage;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.launcher.mummu.cabclient.CabUserApplication;
import com.launcher.mummu.cabclient.utils.FirebaseUtil;

import java.util.Date;

/**
 * Created by muhammed on 2/23/2017.
 */

public class FirebaseStorage {

    public static void insertUserInfo(FirebaseUser user) {
        if (user != null) {
            DatabaseReference fromUrl = CabUserApplication.getFirebaseInstance().getReferenceFromUrl(FirebaseUtil.FIREBASE_URL);
            DatabaseReference child = fromUrl.child(FirebaseUtil.USERS).child(user.getUid());
            child.child(FirebaseUtil.EMAIL).setValue(user.getEmail());
            child.child(FirebaseUtil.NAME).setValue(user.getDisplayName());
            child.child(FirebaseUtil.USER_LOGIN_STATUS).setValue(true);
            child.child(FirebaseUtil.USER_APP_STATUS).setValue(FirebaseUtil.ONLINE);
        }
    }

    public static void updateLoginInfo(String uuid, boolean status) {
        DatabaseReference userChild = getUserChild(uuid);
        userChild.child(FirebaseUtil.USER_LOGIN_STATUS).setValue(status);

    }

    public static DatabaseReference getUserChild(String uuid) {
        DatabaseReference fromUrl = CabUserApplication.getFirebaseInstance().getReferenceFromUrl(FirebaseUtil.FIREBASE_URL);
        DatabaseReference child = fromUrl.child(FirebaseUtil.USERS).child(uuid);
        return child;
    }

    public static void updateLastSeen(Date date, String uuid) {
        getUserChild(uuid).child(FirebaseUtil.USER_LAST_SEEN).setValue(date.toString());
    }

    public static void pushUserComments(String uuId, String comments) {
        DatabaseReference userChild = getUserChild(uuId);
        DatabaseReference reference = getCommentsChild(userChild);
        reference.setValue(comments);
    }

    private static DatabaseReference getCommentsChild(DatabaseReference userChild) {
        DatabaseReference child = userChild.child(FirebaseUtil.COMMENTS).child(System.currentTimeMillis() + "");
        return child;
    }
}
