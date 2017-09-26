package org.rocketsingh.android.rocketsinghbiker.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.format.Time;

import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;

import org.rocketsingh.android.rocketsinghbiker.realmmodels.Trip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mobileresponse.AllRequestResponse;
import mobileresponse.CompleteRequestDetails;

/**
 * Created by Divyu on 9/29/2015.
 */
public class CommonUtilities {
    private static final String LOG_TAG = CommonUtilities.class.getSimpleName();

    private static String getMyPhoneNumber(Context context){
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephonyMgr != null) {
            return mTelephonyMgr.getLine1Number();
        } else {
            return null;
        }
    }

    private static String getMy10DigitPhoneNumber(Context context){
        String s = getMyPhoneNumber(context);
        return s != null && s.length() > 2 ? s.substring(2) : null;
    }


    //The day in the form of a string formatted "December 6"
    public static String getFormattedMonthDay(long dateInMillis ) {
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static boolean getConnectivityStatus(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI)  return true;
            if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) return true;
        }
        return false;
    }

    private static OkHttpClient okHttpClient;
    private static final long DEFAULT_KEEP_ALIVE_DURATION_MS = 5 * 60 * 1000; // 5 min
    private static final int CONNECTION_POOL = 3;
    public static OkHttpClient getSingletonHTTPClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
            String keepAliveDuration = System.getProperty("http.keepAliveDuration");
            long keepAliveDurationMs = keepAliveDuration != null ? Long.parseLong(keepAliveDuration)
                    : DEFAULT_KEEP_ALIVE_DURATION_MS;
            ConnectionPool cPool = new ConnectionPool(CONNECTION_POOL, keepAliveDurationMs);
            okHttpClient.setConnectionPool(cPool);
            okHttpClient.setConnectTimeout(10, TimeUnit.SECONDS);
        }
        return okHttpClient;
    }

    public static class ProtoToPOJO {
        public static List<Trip> convert(AllRequestResponse response) {
            List<Trip> tripList = new ArrayList<>();
            for (CompleteRequestDetails iRequest: response.complete_request) {
                Trip trip = new Trip();
                trip.setOrigin(iRequest.tripData.start_location);
                trip.setDriverName(iRequest.biker_name);
                trip.setTripCost(iRequest.paymentDetails.trip_cost);
                trip.setTripDistance(iRequest.paymentDetails.trip_distance);
                if(iRequest.paymentDetails.trip_time != null) {trip.setTripTime(iRequest.paymentDetails.trip_time.longValue());}
                if (iRequest.time_id != null) {trip.setTimeId(Long.valueOf(iRequest.time_id));}
                trip.setStatus(iRequest.request_status);
                trip.setStartLatitude(iRequest.tripData.start_latitude);
                trip.setStartLongitude(iRequest.tripData.start_longitude);
                trip.setEndLatitude(iRequest.tripData.end_latitude);
                trip.setEndLongitude(iRequest.tripData.end_longitude);

                tripList.add(trip);
            }
            return tripList;
        }
    }
}
