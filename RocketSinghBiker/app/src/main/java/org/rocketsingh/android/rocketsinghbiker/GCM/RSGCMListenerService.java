package org.rocketsingh.android.rocketsinghbiker.GCM;

import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;
import org.rocketsingh.android.rocketsinghbiker.GCM.command.GCMCommand;
import org.rocketsingh.android.rocketsinghbiker.GCM.command.NewTripCommand;
import org.rocketsingh.android.rocketsinghbiker.GCM.command.NotificationDisplayCommand;
import org.rocketsingh.android.rocketsinghbiker.GCM.command.ProfessionalSyncCommand;
import org.rocketsingh.android.rocketsinghbiker.GCM.command.TripCancelledCommand;
import org.rocketsingh.android.rocketsinghbiker.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghbiker.utilities.Constants;

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
    private static final String GCM_MESSAGETYPE_NEWREQUEST = "newrequest";
    private static final String GCM_MESSAGETYPE_REQUESTCANCELLED = "tripcancelled";

    private static final Map<String, GCMCommand> GCM_MESSAGE_RECEIVERS;
    static {
        Map<String, GCMCommand> receivers = new HashMap<>();
        receivers.put(GCM_MESSAGETYPE_NOTIFICATION, new NotificationDisplayCommand());
        receivers.put(GCM_MESSAGETYPE_SYNCPROFESSIONALS, new ProfessionalSyncCommand());
        receivers.put(GCM_MESSAGETYPE_NEWREQUEST, new NewTripCommand());
        receivers.put(GCM_MESSAGETYPE_REQUESTCANCELLED, new TripCancelledCommand());
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
                NotificationDisplayCommand.NotificationData notificationData = new NotificationDisplayCommand.NotificationData();
                notificationData.tickrText = data.getString("tickerText");
                notificationData.contentText = data.getString("contentText");
                notificationData.contentTitle = data.getString("contentTitle");
                notificationData.uri = data.getString("uri");

                jitterupperBound = Long.valueOf(data.getString("jitterupperBound"));
                command.execute(this, jitterupperBound, notificationData);
            } else if (type.equals(GCM_MESSAGETYPE_NEWREQUEST)) {
                Trip trip = new Trip();
                trip.setCustomerName(message.getString("Name"));
                trip.setEntityId(message.getString("EntityID"));
                trip.setEntityType(message.getString("EntityType"));
                trip.setTimeId(message.getLong("TimeID"));
                trip.setCustomerLatitude(message.getDouble("Cur_lat"));
                trip.setCustomerLongitude(message.getDouble("Cur_long"));
                trip.setStatus(Constants.TripStatusCode.TRIP_ASSIGNED);
                command.execute(this, jitterupperBound, trip);
            } else if (type.equals(GCM_MESSAGETYPE_SYNCPROFESSIONALS)) {
                jitterupperBound = Long.valueOf(data.getString("jitterupperBound"));
                command.execute(this, jitterupperBound, null);
            } else if (type.equals(GCM_MESSAGETYPE_REQUESTCANCELLED)) {
                command.execute(this, jitterupperBound, null);
            } else {
                Log.i(LOG_TAG, "Received Notification but no Type matched");
            }
        } catch (JSONException e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "JSONException logged while receiving Notification");
            Crashlytics.logException(e);
            Log.i(LOG_TAG, "JSONException logged while receiving Notification" + e.getMessage());
            e.printStackTrace();
        } catch(Exception e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "While Receiving Notification");
            Crashlytics.logException(e);
            Log.i(LOG_TAG, "Exception logged while receiving Notification" + e.getMessage());
            e.printStackTrace();
        }

    }//End of onMessageReceived()

}
