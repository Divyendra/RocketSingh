package org.rocketsingh.android.rocketsinghbiker.GCM.command;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.rocketsingh.android.rocketsinghbiker.R;
import org.rocketsingh.android.rocketsinghbiker.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghbiker.ui.BikerMap.BikerMapActivity;
import org.rocketsingh.android.rocketsinghbiker.utilities.Constants;
import org.rocketsingh.android.rocketsinghbiker.utilities.SharedPrefHelper;

import io.realm.Realm;

/**
 * Created by Divyu on 10/20/2015.
 */
public class TripCancelledCommand extends GCMCommand {
    private static final int NOTIFICATION_ID = 2;
    private static final String LOG_TAG = NewTripCommand.class.getSimpleName();
    Uri soundUri = Uri.parse("android.resource://org.rocketsingh.android.rocketsinghbiker/" + R.raw.cancelrequest_notif);
    private static long[] mVibratePattern = {500,500,500,500,500,500,500,500,500};

    @Override
    public void execute(Context context, long syncJitter, Object extraData) {
        Realm realm = Realm.getInstance(context);
        Trip trip = realm.where(Trip.class).equalTo("status", Constants.TripStatusCode.TRIP_ASSIGNED)
                .or().equalTo("status", Constants.TripStatusCode.TRIP_ACTIVE)
                .or().equalTo("status", Constants.TripStatusCode.TRIP_STARTED).findFirst();
        if (trip.getStatus() == Constants.TripStatusCode.TRIP_STARTED) {
            Crashlytics.logException(new IllegalStateException("Cancelling a started trip." +
                    " This should not happen normally. TimeID: " + trip.getTimeId()));
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "Conditional Exception not working in Server side properly");
        }
        SharedPrefHelper.Realm_Trips.setInsertType(context, SharedPrefHelper.Realm_Trips.TRIP_CANCELLEED);
        realm.beginTransaction();
        trip.setStatus(Constants.TripStatusCode.TRIP_CANCELLED);
        realm.commitTransaction();

        Intent mNotifIntent = new Intent(context, BikerMapActivity.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 0, mNotifIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

        Log.i(LOG_TAG, "Starting a Notification");
        NotificationCompat.Builder  builder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setTicker("TRIP CANCELLED")
                .setContentTitle("You Trip has been cancelled by the Customer")
                .setSound(soundUri)
                .setVibrate(mVibratePattern)
                .setSmallIcon(R.drawable.abc_popup_background_mtrl_mult)
                .setContentIntent(mPendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
