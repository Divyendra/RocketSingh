package org.rocketsingh.android.rocketsinghbiker.GCM.command;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.rocketsingh.android.rocketsinghbiker.R;
import org.rocketsingh.android.rocketsinghbiker.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghbiker.ui.BikerMap.BikerMapActivity;
import org.rocketsingh.android.rocketsinghbiker.utilities.SharedPrefHelper;

import io.realm.Realm;

/**
 * Created by Divyu on 10/9/2015.
 */
public class NewTripCommand extends GCMCommand {
    private static final int NOTIFICATION_ID = 1;
    private static final String LOG_TAG = NewTripCommand.class.getSimpleName();
    Uri soundUri = Uri.parse("android.resource://org.rocketsingh.android.rocketsinghbiker/" + R.raw.alarm_notif);
    private static long[] mVibratePattern = {500,500,500,500,500,500,500,500,500};

    @Override
    public void execute(Context context, long syncJitter, Object extraData) {
        Log.i(LOG_TAG, "New trip added");
        SharedPrefHelper.Realm_Trips.setInsertType(context, SharedPrefHelper.Realm_Trips.NEWTRIP);
        Realm realm = Realm.getInstance(context);
        Trip trip = (Trip)extraData;
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(trip);
        realm.commitTransaction();
        Intent mNotifIntent = new Intent(context, BikerMapActivity.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 0, mNotifIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

        Log.i(LOG_TAG, "Starting a Notification");
        NotificationCompat.Builder  builder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setTicker("NEW TRIP")
                .setContentTitle("You have a New Trip")
                .setSound(soundUri)
                .setVibrate(mVibratePattern)
                .setSmallIcon(R.drawable.abc_popup_background_mtrl_mult)
                .setContentIntent(mPendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

    }
}
