package org.rocketsingh.android.rocketsinghpillion.GCM;

import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;
import org.rocketsingh.android.rocketsinghpillion.GCM.command.GCMCommand;
import org.rocketsingh.android.rocketsinghpillion.GCM.command.NotificationDisplayCommand;
import org.rocketsingh.android.rocketsinghpillion.GCM.command.ProfessionalSyncCommand;
import org.rocketsingh.android.rocketsinghpillion.GCM.command.TripEndedCommand;
import org.rocketsingh.android.rocketsinghpillion.GCM.command.TripStartedCommand;
import org.rocketsingh.android.rocketsinghpillion.realmmodels.Biker;
import org.rocketsingh.android.rocketsinghpillion.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghpillion.utilities.Constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Divyu on 7/1/2015.
 */
//GCMListenerService which enables various aspects of handling messages such as detecting different downstream message types.
public class RSGCMListenerService extends GcmListenerService {

    private static final String LOG_TAG = RSGCMListenerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 0;
    private long[] mVibratePattern = { 0, 200, 200, 300 };

    private static final String GCM_MESSAGETYPE_NOTIFICATION = "notification";
    private static final String GCM_MESSAGETYPE_SYNCPROFESSIONALS = "syncprofessionals";
    private static final String GCM_MESSAGETYPE_TRIPSTARTED = "tripstarted";
    private static final String GCM_MESSAGETYPE_TRIPENDED = "tripended";

    private static final Map<String, GCMCommand> GCM_MESSAGE_RECEIVERS;
    static {
        Map<String, GCMCommand> receivers = new HashMap<>();
        receivers.put(GCM_MESSAGETYPE_NOTIFICATION, new NotificationDisplayCommand());
        receivers.put(GCM_MESSAGETYPE_SYNCPROFESSIONALS, new ProfessionalSyncCommand());
        receivers.put(GCM_MESSAGETYPE_TRIPSTARTED, new TripStartedCommand());
        receivers.put(GCM_MESSAGETYPE_TRIPENDED, new TripEndedCommand());
        GCM_MESSAGE_RECEIVERS = Collections.unmodifiableMap(receivers);
    }

    //'from' => SenderID of the sender.
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Set<String> keys = data.keySet();

        Log.i(LOG_TAG, "onMessageReceived()");
        Log.d(LOG_TAG, "From: " + from);
        for (String key: keys) {
            Log.d(LOG_TAG, "Key: " + key + "- Message: " + data.get(key).toString());
        }

        try {
            JSONObject message = new JSONObject(data.getString("message"));
            Log.i(LOG_TAG, "Sample JSON : " + message.toString());
            if (!message.has("type")) {
                Log.i(LOG_TAG, "not type key in JSON key 'message'");
                return;
            }
            String type = message.getString("type");
            long jitterupperBound = 0L;
            GCMCommand command = GCM_MESSAGE_RECEIVERS.get(type);
        if (type.equals(GCM_MESSAGETYPE_NOTIFICATION)) {
            NotificationDisplayCommand.NotificationData notificationData= new NotificationDisplayCommand.NotificationData();
            notificationData.tickrText = message.getString("tickerText");
            notificationData.contentText = message.getString("contentText");
            notificationData.contentTitle = message.getString("contentTitle");
            notificationData.uri = message.getString("uri");

            jitterupperBound = Long.valueOf(data.getString("jitterupperBound"));
            command.execute(this, jitterupperBound, notificationData);
        } else if (type.equals(GCM_MESSAGETYPE_TRIPSTARTED)) {
            Long timeId = message.getLong("TimeID");
            command.execute(this, jitterupperBound, timeId);
        } else if (type.equals(GCM_MESSAGETYPE_TRIPENDED)) {
            Trip trip = new Trip();
            Log.i(LOG_TAG, "timeID: " + message.getString("TimeID") + "; cost: " + message.getDouble("Total_cost") + "; distance: " +
                    message.getString("Total_distance"));
            trip.setTimeId(message.getLong("TimeID"));
            trip.setTripTime(message.getLong("Total_time"));
            trip.setTripCost(message.getDouble("Total_cost"));
            trip.setTripDistance(data.getDouble("Total_distance"));
            command.execute(this, jitterupperBound, trip);
        } else {
            Log.i(LOG_TAG, "Received Notification but no Type matched");
        }
        } catch (JSONException e) {
            Log.i(LOG_TAG, "JSONException logged while receiving Notification" + e.getMessage());
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "Failed to complete token refresh");
            Crashlytics.logException(e);
            e.printStackTrace();
        } catch(Exception e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "Failed to complete token refresh");
            Crashlytics.logException(e);
            Log.i(LOG_TAG, "Exception logged while receiving Notification" + e.getMessage());
            e.printStackTrace();
        }

    }//End of onMessageReceived()

}
