package org.rocketsingh.android.rocketsinghpillion.GCM.command;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.rocketsingh.android.rocketsinghpillion.GCM.TriggerSyncReceiver;
import java.util.Random;

/**
 * Created by Divyu on 7/1/2015.
 */
public class ProfessionalSyncCommand extends GCMCommand {
    private static final String LOG_TAG = ProfessionalSyncCommand.class.getSimpleName();
    private static final int DEFAULT_TRIGGER_SYNC_MAX_JITTER_MILLIS = 15 * 60 * 1000; // 15 minutes
    private static final Random RANDOM = new Random();

    @Override
    public void execute(Context context, long syncJitter, Object extraData) {
        if (syncJitter == 0) {
            syncJitter = DEFAULT_TRIGGER_SYNC_MAX_JITTER_MILLIS;
        }

        scheduleSync(context, syncJitter);
    }

    private void scheduleSync(Context context, long syncJitter) {
        syncJitter = (long) (RANDOM.nextFloat() * syncJitter);
        Log.i(LOG_TAG, "Schedule next sync in " + syncJitter + "ms");

        Intent syncAdapterTriggerIntent = new Intent(context, TriggerSyncReceiver.class);
        //Incase of categorising this intent is for ProfessionalSync, keep data in 'intent' to differentiate
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, syncAdapterTriggerIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
            .set(AlarmManager.RTC, System.currentTimeMillis() + syncJitter, pendingIntent);

    }
}
