package org.rocketsingh.android.rocketsinghpillion.GCM.command;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.rocketsingh.android.rocketsinghpillion.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghpillion.utilities.Constants;
import org.rocketsingh.android.rocketsinghpillion.utilities.SharedPrefHelper;

import in.raveesh.pacemaker.Pacemaker;
import io.realm.Realm;

/**
 * Created by Divyu on 10/11/2015.
 */
public class TripEndedCommand extends GCMCommand {
    private static final String LOG_TAG = TripEndedCommand.class.getSimpleName();

    @Override
    public void execute(Context context, long syncJitter, Object extraData) {
        Trip reference = (Trip) extraData;
        Realm realm = Realm.getInstance(context);
        Trip trip = realm.where(Trip.class).equalTo("timeId", reference.getTimeId()).findFirst();
        if (trip != null) {
            Log.i(LOG_TAG, "TripDistance: " + reference.getTripDistance() + ", TripTime: " + reference.getTripTime());
            SharedPrefHelper.Realm_Trips.putInsertType(context, SharedPrefHelper.Realm_Trips.TRIPUPDATE_FINISHED);
            realm.beginTransaction();
            trip.setStatus(Constants.TripStatusCode.TRIP_COMPLETED);
            trip.setTripCost(reference.getTripCost());
            trip.setTripDistance(reference.getTripDistance());
            trip.setTripTime(reference.getTripTime());
            realm.commitTransaction();
            SharedPrefHelper.setKey_isOnTrip(context, false);
        } else {
            Crashlytics.logException(new NullPointerException("Trip with the given TimeId is not present.-->TripEndedCommand"));
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "TripEndedCommand");
        }
        //Cancels the 'RepeatedAlarm' that sends a broadcast to Play Services to send GCM Heartbeats.
        Pacemaker.cancelLinear(context, Constants.GCM_HEARTBEAT_INTERVAL);
        realm.close();
    }
}
