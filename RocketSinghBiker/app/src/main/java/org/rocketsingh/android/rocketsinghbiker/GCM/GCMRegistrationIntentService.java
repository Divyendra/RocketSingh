package org.rocketsingh.android.rocketsinghbiker.GCM;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.rocketsingh.android.rocketsinghbiker.utilities.SharedPrefHelper;

import java.io.IOException;

/**
 * Created by Divyu on 6/30/2015.
 */
public class GCMRegistrationIntentService extends IntentService {

    private static final String LOG_TAG = GCMRegistrationIntentService.class.getSimpleName();
    private static final String[] TOPICS = {"global"};
    public static final String PREF_KEY_GCMREGISTRATION = "gcmregistration";
    public static final String INTENTACTION_REGISTRATIONCOMPLETE = "registrationcomplete";
    public static final String PROJECT_ID = "983022026486"; //ProjectId for RocketSingh.

    public GCMRegistrationIntentService() {
        super("GCMRegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {

            //Only one thread can execute inside a Java code block synchronized on the same monitor object.
            //Useful in the unlikely event that multiple refreshs occur simultaneously.
            synchronized (LOG_TAG) {
                // Initially this call goes out to the network to retrieve the token, subsequent calls are local
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(PROJECT_ID,
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                Log.i(LOG_TAG, "Token generated is :" + token);
                SharedPrefHelper.setKey_Gcmtoken(this, token);

                sendRegistrationToServer(token);

                //subscribeTopics(token);

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
                //http://stackoverflow.com/a/5960732/4144209 for why apply() is used instead of commit()
                sharedPreferences.edit().putBoolean(PREF_KEY_GCMREGISTRATION, true).commit();
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to complete token refresh", e);
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "Failed to complete TokenRefresh");
            Crashlytics.logException(e);
            sharedPreferences.edit().putBoolean(PREF_KEY_GCMREGISTRATION, false).commit();
        }

        Intent regComplete = new Intent(INTENTACTION_REGISTRATIONCOMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(regComplete);
    }

    private void sendRegistrationToServer(String token) {
        // TODO
    }

    /**
     *Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
    * @throws IOException if unable to reach the GCM PubSub service
    */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

}
