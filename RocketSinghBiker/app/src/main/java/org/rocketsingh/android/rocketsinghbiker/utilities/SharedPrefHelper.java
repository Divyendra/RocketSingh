package org.rocketsingh.android.rocketsinghbiker.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import org.rocketsingh.android.rocketsinghbiker.R;

/**
 * Created by Divyu on 9/29/2015.
 */
public class SharedPrefHelper {
    //Note: PreferenceManager.getDefaultSharedPreferences(getActivity()) uses PRIVATE MODE only.
    //Below are preference key values
    private static final String key_isLoggedIn = "ISLOGGEDIN";
    private static final String key_gcmtoken = "GCMTOKEN";
    private static final String key_customerID = "CUSTOMERID";
    //BikerStatus is the only field which is stored in SharedPref. Rest all BikerDetails would be in Realm.
    //This flag is only 'updated' in the Services(TripUpdation, LocationUpdation), but queried in other Activities as well.
//    private static final String key_bikerstatus = "BIKERSTATUS";
    private static final String key_destchangedflag = "DESTCHANGEDFLAG";

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

    public static String getKey_CustomerID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key_customerID, "2");
    }

    public static boolean setIsDestChanged(Context context, boolean status) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key_destchangedflag, status).commit();
    }

    public static boolean getIsDestChanged(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key_destchangedflag, false);
    }

    public static boolean setLatestLatLng(Context context, double lat, double lon) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(context.getString(R.string.pref_key_currentlat), String.valueOf(lat));
        editor.putString(context.getString(R.string.pref_key_currentlong), String.valueOf(lon));
        return editor.commit();
    }

    public static Location getLastLocation(Context context) {
        String lat = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_currentlat), "0");
        String lon = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_currentlong), "0");
        Location location = new Location("SharedPrefs");
        location.setLatitude(Double.valueOf(lat));
        location.setLongitude(Double.valueOf(lon));
        return location;
    }

    public static class Realm_Trips {
        public static final int NEWHISTORYTRIP = 1;
        public static final int FULLHISTORYSYNC = 2;
        public static final int NEWTRIP = 3;
        public static final int TRIP_STARTED = 4;
        public static final int TRIP_CANCELLEED = 5;

        private static final String key = "Realm_Trips";

        public static int getInsertType(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(Realm_Trips.key, NEWHISTORYTRIP);
        }

        public static boolean setInsertType(Context context, int value) {
            return PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Realm_Trips.key, value).commit();
        }
    }

}
