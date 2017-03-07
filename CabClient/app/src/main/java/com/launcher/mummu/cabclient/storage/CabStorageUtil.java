package com.launcher.mummu.cabclient.storage;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.launcher.mummu.cabclient.utils.DateDiff;

import java.util.Calendar;

/**
 * Created by muhammed on 2/20/2017.
 */

public class CabStorageUtil {
    public static final String USER_NAME = "username";
    public static final String IS_LOGGED = "is_logged";
    public static final String LOCATION_NAME = "location_name";
    public static final String LOCATION = "location";
    public static final String LOCATION_LAT = "location_lat";
    public static final String LOCATION_LONG = "location_long";
    public static final String LOCATION_LAT_LONG = "location_latlong";
    public static final String IS_NOTIFICATION_ON = "is_notification_on";
    public static final String USER_UUID = "uuid";
    public static final String USER_DISPLAY_NAME = "user_display_name";
    private static final String MORNING_NOTIFICATION = "morning_notification";
    private static final String EVENING_NOTIFICATION = "evening_notification";
    private static final String IS_DIALOG_SHOWED = "is_dialog_showed";
    private static final String DIALOG_TIME = "dialog_time";
    private static final int MORNING_TIME_START = 9;
    private static final int MORNING_TIME_END = 11;
    private static final int EVENING_TIME_START = 7;
    private static final int EVENING_TIME_END = 8;
    private static final String IS_FIRST_TIME_DILAOG = "is_first_time_dilaog";
    private static final String USER_IMAGE = "user_image";
    private static final String NOTIFICATION_KILOMETER_RANGE = "notification_km_range";
    private static final String IS_TRAFFIC_ENABLED = "is_traffic_enabled";

    public static void setUsername(Context context, String key, String value) {
        CabStorage.insertStringData(context, key, value);
        setLogging(context, IS_LOGGED, true);

    }

    public static boolean isLogged(Context context, String key) {
        return CabStorage.getBooleanDataDefaultFalse(context, key);
    }

    public static String getUsername(Context context, String key) {
        return CabStorage.getStringData(context, key);
    }

    public static void setLogging(Context context, String key, boolean value) {
        CabStorage.insertBooleanData(context, key, value);
    }

    public static void storeLocation(Context context, String key, String value) {
        CabStorage.insertStringData(context, key, value);
    }

    public static String getLocation(Context context, String key) {
        return CabStorage.getStringData(context, key);
    }

    public static LatLng getLocationLatLng(Context context) {
        LatLng latLng = null;
        try {
            String locationLat = CabStorageUtil.getLocation(context, CabStorageUtil.LOCATION_LAT);
            String locationLong = CabStorageUtil.getLocation(context, CabStorageUtil.LOCATION_LONG);
            Double lat = Double.parseDouble(locationLat);
            Double longetude = Double.parseDouble(locationLong);
            latLng = new LatLng(lat, longetude);
        } catch (Exception e) {

        }
        return latLng;
    }

    public static boolean isNotificationOn(Context context) {
        return CabStorage.getBooleanDataDefaultTrue(context, IS_NOTIFICATION_ON);
    }

    public static void setNotificationStatus(Context context, boolean isChecked) {
        CabStorage.insertBooleanData(context, IS_NOTIFICATION_ON, isChecked);
    }

    public static void setMorningNotificationStatus(Context context, boolean isChecked) {
        CabStorage.insertBooleanData(context, MORNING_NOTIFICATION, isChecked);
    }

    public static void setEveningNotificationStatus(Context context, boolean isChecked) {
        CabStorage.insertBooleanData(context, EVENING_NOTIFICATION, isChecked);
    }

    public static boolean isMorningChecked(Context context) {
        return CabStorage.getBooleanDataDefaultTrue(context, MORNING_NOTIFICATION);
    }


    public static boolean isEveningChecked(Context context) {
        return CabStorage.getBooleanDataDefaultTrue(context, EVENING_NOTIFICATION);
    }

    public static void storeDialogPref(Context context, boolean isShowed) {
        CabStorage.insertBooleanData(context, IS_DIALOG_SHOWED, isShowed);
    }

    public static void storeDialogTime(Context context, long timeMillis) {
        CabStorage.insertLongData(context, DIALOG_TIME, timeMillis);
    }

    public static boolean isDialogShowedToday(Context context) {
        return CabStorage.getBooleanDataDefaultFalse(context, IS_DIALOG_SHOWED);
    }

    public static boolean isTodayMorningShow(Context context) {
        monitorMorning(context);
        if (!isMorningChecked(context)) {
            return false;
        }
        if (isMorningTime()) {
            return isOverRemindDateMorning(context);

        }
        return false;
    }

    public static boolean isTodayEveningShow(Context context) {
        monitorEvening(context);
        if (!isEveningChecked(context)) {
            return false;
        }
        if (isEveningTime()) {
            return isOverRemindDateEvening(context);
        }
        return false;
    }


    private static boolean isOverRemindDateEvening(Context context) {
        return DateDiff.isOverDate(PreferenceHelperEvening.getRemindInterval(context), 1);
    }

    private static void monitorEvening(Context context) {
        if (PreferenceHelperEvening.isFirstLaunch(context)) {
            PreferenceHelperEvening.setInstallDate(context);
        }
    }


    private static boolean isMorningTime() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if (timeOfDay >= MORNING_TIME_START && timeOfDay < MORNING_TIME_END) {
            return true;
        } else {
            return false;

        }
    }


    private static boolean isEveningTime() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if (timeOfDay >= EVENING_TIME_START && timeOfDay < EVENING_TIME_END) {
            return true;
        } else {
            return false;

        }
    }

    private static void monitorMorning(Context context) {
        if (PreferenceHelperMorning.isFirstLaunch(context)) {
            PreferenceHelperMorning.setInstallDate(context);
        }
    }

    private static boolean isOverRemindDateMorning(Context context) {
        return DateDiff.isOverDate(PreferenceHelperMorning.getRemindInterval(context), 1);
    }

    public static void setUUid(Context context, String key, String value) {
        CabStorage.insertStringData(context, key, value);
    }

    public static String getUUId(Context context) {
        return CabStorage.getStringData(context, USER_UUID);
    }

    public static void setUserDisplayName(Context context, String value) {
        CabStorage.insertStringData(context, USER_DISPLAY_NAME, value);
    }

    public static String getUserDisplayName(Context context) {
        return CabStorage.getStringData(context, USER_DISPLAY_NAME);
    }

    public static boolean isFirstTime(Context context) {
        return CabStorage.getBooleanDataDefaultFalse(context, IS_FIRST_TIME_DILAOG);
    }

    public static void setFirstTime(Context context, boolean value) {
        CabStorage.insertBooleanData(context, IS_FIRST_TIME_DILAOG, value);
    }

    public static void setUserProfile(Context context, Uri photoUrl) {
        CabStorage.insertStringData(context, USER_IMAGE, photoUrl.toString());
    }

    public static String getUserProfile(Context context) {
        return CabStorage.getStringData(context, USER_IMAGE);
    }

    public static void clearAll(Context context) {
        CabStorage.clearAll(context);
    }

    public static void setNotificationKilometerRange(Context context, long oneKilomter) {
        CabStorage.insertLongData(context, NOTIFICATION_KILOMETER_RANGE, oneKilomter);
    }

    public static long getNotificationKilometerRange(Context context) {
        return CabStorage.getLongData(context, NOTIFICATION_KILOMETER_RANGE);
    }

    public static boolean isTrafficEnabled(Context context) {
        return CabStorage.getBooleanDataDefaultTrue(context, IS_TRAFFIC_ENABLED);
    }

    public static void setTrafficEnabled(Context context, boolean isChecked) {
        CabStorage.insertBooleanData(context, IS_TRAFFIC_ENABLED, isChecked);
    }
}
