package org.rocketsingh.android.rocketsinghbiker.GCM.command;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.rocketsingh.android.rocketsinghbiker.R;
import org.rocketsingh.android.rocketsinghbiker.ui.BikerMap.BikerMapActivity;


/**
 * Created by Divyu on 7/1/2015.
 */
public class NotificationDisplayCommand extends GCMCommand {
    private static final String LOG_TAG = NotificationDisplayCommand.class.getSimpleName();
    private static final int DEFAULT_TRIGGER_SYNC_MAX_JITTER_MILLIS = 0;
    private static long[] mVibratePattern = { 0, 200, 200, 300 };
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void execute(Context context, long syncJitter, Object extraData) {
        if (syncJitter < 0) syncJitter = DEFAULT_TRIGGER_SYNC_MAX_JITTER_MILLIS;

        if (extraData != null) {
            //syncJitter as of now is just being set but it's role is nothing for 'Noticifications' .Have to work on it later.
            sendNotification(syncJitter, context, (NotificationData) extraData);
        }

    }

    private void sendNotification(long syncJitter, Context context, NotificationData notificationData) {
        Intent intent = null;
        if (notificationData.uri != null && !notificationData.uri.isEmpty()) {
            intent  = new Intent(Intent.ACTION_VIEW, Uri.parse(notificationData.uri) );
        } else {
            intent = new Intent(context, BikerMapActivity.class);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Log.i(LOG_TAG, "Triggered Notification in " + syncJitter + " ms");
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setTicker(notificationData.tickrText)
                .setWhen(syncJitter)
                .setContentTitle(notificationData.contentTitle)
                .setContentText(notificationData.contentText)
                .setVibrate(mVibratePattern)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notifBuilder.build());
    }

    public static class NotificationData {
        public String tickrText;
        public String contentTitle;
        public String contentText;
        public String smallImageURL;
        public String largeImageURL;
        public String uri; // if null, would send Notifcation to MainActivity.
    }

}
