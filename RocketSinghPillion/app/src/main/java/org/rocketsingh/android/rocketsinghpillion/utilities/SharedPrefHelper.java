package org.rocketsingh.android.rocketsinghpillion.utilities;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Divyu on 9/29/2015.
 */
public class SharedPrefHelper {
    //Note: PreferenceManager.getDefaultSharedPreferences(getActivity()) uses PRIVATE MODE only.
    //Below are preference key values
    private static final String key_isLoggedIn = "ISLOGGEDIN";
    private static final String key_gcmtoken = "GCMTOKEN";
    private static final String key_isOnTrip = "ISONTRIPFLAG";

    public static boolean isLoggedIn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key_isLoggedIn, false);
    }

    public static boolean setKey_isLoggedIn(Context context, boolean value) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key_isLoggedIn, value).commit();
    }

    public static boolean setKey_Gcmtoken(Context context, String gcmToken) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key_gcmtoken, gcmToken).commit();
    }

    public static String getKey_Gcmtoken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key_gcmtoken, "");
    }


    public static boolean setKey_isOnTrip(Context context, boolean value) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key_isOnTrip, value).commit();
    }

    public static boolean isOnTrip(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key_isOnTrip, false);
    }

    public static class Realm_Trips {
        public static final int NEWHISTORYTRIP = 1;
        public static final int FULLHISTORYSSYNC = 2;
        public static final int NEWTRIP = 3;
        public static final int TRIPUPDATE_STARTED = 4;
        public static final int TRIPUPDATE_FINISHED = 5;

        private static final String key = "TRIP";

        public static int getInsertType(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(Realm_Trips.key, NEWHISTORYTRIP);
        }

        public static boolean putInsertType(Context context, int value) {
            return PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Realm_Trips.key, value).commit();
        }
    }

}
