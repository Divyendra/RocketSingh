package org.rocketsingh.android.rocketsinghpillion.GCM.command;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.rocketsingh.android.rocketsinghpillion.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghpillion.utilities.Constants;
import org.rocketsingh.android.rocketsinghpillion.utilities.SharedPrefHelper;

import in.raveesh.pacemaker.Pacemaker;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;

/**
 * Created by Divyu on 10/11/2015.
 */
public class TripStartedCommand extends GCMCommand {
    private static final String LOG_TAG = TripStartedCommand.class.getSimpleName();

    @Override
    public void execute(Context context, long syncJitter, Object extraData) {
        Long timeId = (Long) extraData;
        Realm realm = Realm.getInstance(context);
        Trip trip = realm.where(Trip.class).equalTo("timeId", timeId).findFirst();
        Log.i(LOG_TAG, "TripStart received-1");
        if (trip != null) {
            Log.i(LOG_TAG, "TripStart received-2");
            SharedPrefHelper.Realm_Trips.putInsertType(context, SharedPrefHelper.Realm_Trips.TRIPUPDATE_STARTED);
            realm.beginTransaction();
            trip.setStatus(Constants.TripStatusCode.TRIP_STARTED);
            realm.commitTransaction();
        } else {
            Crashlytics.logException(new NullPointerException("Trip with the given TimeId is not present.-->TripStartedCommand"));
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "TripStartedCommand");
        }
        realm.close();
    }
}
