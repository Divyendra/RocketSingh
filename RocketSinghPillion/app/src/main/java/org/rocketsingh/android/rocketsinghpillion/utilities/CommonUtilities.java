package org.rocketsingh.android.rocketsinghpillion.utilities;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import org.rocketsingh.android.rocketsinghpillion.R;
import org.rocketsingh.android.rocketsinghpillion.realmmodels.Biker;
import org.rocketsingh.android.rocketsinghpillion.realmmodels.Trip;
import mobileresponse.AllRequestResponse;
import mobileresponse.CompleteRequestDetails;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

    public static View getProgressBar(Context context, String progressBarText) {
        View v = LayoutInflater.from(context).inflate(R.layout.progressbar_layout, null, false);
        TextView tvProgress = (TextView)v.findViewById(R.id.progressBarText);
        tvProgress.setText(progressBarText);
        return v;
    }

    public static class ProtoToPOJO {
        public static List<Trip> convert(AllRequestResponse response) {
            List<Trip> tripList = new ArrayList<>();
            for (CompleteRequestDetails iRequest: response.complete_request) {
                Trip trip = new Trip();
                if (iRequest.tripData == null) {
                    Crashlytics.setString("WHERE", LOG_TAG);
                    Crashlytics.setString("MESSAGE", "Server sent Tripdata object as null in History");
                    Crashlytics.logException(new NullPointerException());
                }
                trip.setOrigin(iRequest.tripData.start_location);
                Biker biker = new Biker();
                biker.setName(iRequest.biker_name);
                trip.setBikerAlloted(biker);
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
