package org.rocketsingh.android.rocketsinghbiker.GCM;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by Divyu on 7/1/2015.
 */
//Useful for the random jitter fetch setup - jitteredtimeAlarmBroadcasts and triggers Syncs
public class TriggerSyncReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Incase of multiple Sync requests, use data in 'intent' for differentiating
        //SunshineSyncAdapter.syncImmediately(context);
    }
}
