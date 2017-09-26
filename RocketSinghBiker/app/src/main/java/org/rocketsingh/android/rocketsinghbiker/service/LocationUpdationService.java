package org.rocketsingh.android.rocketsinghbiker.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.rocketsingh.android.rocketsinghbiker.R;
import org.rocketsingh.android.rocketsinghbiker.realmmodels.BikerDetail;
import org.rocketsingh.android.rocketsinghbiker.ui.BikerMap.BikerMapActivity;
import org.rocketsingh.android.rocketsinghbiker.utilities.CommonUtilities;
import org.rocketsingh.android.rocketsinghbiker.utilities.Constants;
import org.rocketsingh.android.rocketsinghbiker.utilities.SharedPrefHelper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import biker.BikerLocation;
import mobilerequest.BikerStatusData;
import io.realm.Realm;

public class LocationUpdationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected GoogleApiClient mGoogleApiClient;
    protected boolean mFlagResolvingError = false;
    protected boolean mFlagGooglePlayConnectionPresent = false;
    private boolean mRequestingLocationUpdates; //Used as flag for not sending LocationRequests twice.
    private LocationRequest mLocationRequest;

    public static final long  UPDATE_INTERVAL_IN_MS = 6000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MS = 5000;
    public static final long BIKER_HEARTBEAT_INTERVAL_IN_MS = 2 * 60 * 1000;
    private long mLastHeartBeatSent;
    private static final Intent GTALK_HEART_BEAT_INTENT = new Intent("com.google.android.intent.action.GTALK_HEARTBEAT");
    private static final Intent MCS_MCS_HEARTBEAT_INTENT = new Intent("com.google.android.intent.action.MCS_HEARTBEAT");

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private HandlerThread handlerThread;


    private static final String LOG_TAG = LocationUpdationService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 100;
    private static final String CHANGE_VISIBILITY_ACTION_INTENT = "CHANGE_VISIBILITY_ACTION_INTENT";

    private String LOCATIONUPDATEURL;
    private String GOOFFLINEURL;

    private static final int HANDLEMSG_UPDATELOCATION = 1;
    private static final int HANDLEMSG_GO_OFFLINE = 2;

    //Flag to start Service in BikerMap Activity if not started yet.
    private static boolean isServiceRunning;
    //Flag to send Status to server only the first time
    private static boolean isNotFirstTime;

    private final class ServiceHandler extends Handler {
        long lastSentTime;
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            Realm realm = Realm.getInstance(LocationUpdationService.this);
            BikerDetail bikerDetail = realm.where(BikerDetail.class).findFirst();
            // Normally we would do some work here, like download a file.
            switch (msg.what) {
                case HANDLEMSG_UPDATELOCATION:
                    Location location = (Location) msg.obj;
                    Log.i(LOG_TAG, "Handler executing message for location:(" + location.getLatitude() + ", " + location.getLongitude() + ")");
                    if ((System.currentTimeMillis() - lastSentTime) >= FASTEST_UPDATE_INTERVAL_IN_MS) {
                        synchronized (this) {
                            try {
                                //Sending biker location data here through a HTTP POST
                                BikerLocation.Builder bikerLocationBuilder = new BikerLocation.Builder()
                                        .selfie_url("")
                                        .first_name(bikerDetail.getName())
                                        .last_name("")
                                        .bike_license_plate(bikerDetail.getBikeLicensePlate())
                                        .phone(bikerDetail.getPhoneNo())
                                        .alternate_phone("")
                                        .biker_type(bikerDetail.getBikerType())
                                        .current_lat(location.getLatitude())
                                        .current_long(location.getLongitude())
                                        .biker_device_token(SharedPrefHelper.getKey_Gcmtoken(LocationUpdationService.this))
                                        .date_created(bikerDetail.getDateCreated());  //date at which the biker account is created

                                if (!isNotFirstTime) {
                                    isNotFirstTime = true;
                                    bikerLocationBuilder.bikerStatus(Constants.BikerStatusFlags.BIKER_LOGIN_AVAILABLE);
                                }

                                sendProto(bikerLocationBuilder.build().toByteArray(), LOCATIONUPDATEURL);
                                lastSentTime = System.currentTimeMillis();
                            } catch (Exception e) {
                                Crashlytics.setString("WHERE", LOG_TAG);
                                Crashlytics.setString("MESSAGE", "While Updating Location in Bgnd Handler");
                                Crashlytics.logException(e);
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case HANDLEMSG_GO_OFFLINE:
                    BikerStatusData bikerStatusData = new BikerStatusData.Builder()
                            .biker_id(bikerDetail.getPhoneNo())
                            .biker_status_flag(Constants.BikerStatusFlags.BIKER_LOGOUT).build();
                    if (sendProto(bikerStatusData.toByteArray(), GOOFFLINEURL)) {
                        isServiceRunning = false;
                        isNotFirstTime = false;
                        stopForeground(true);
                        stopSelf();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed. Please check Internet", Toast.LENGTH_LONG).show();
                        Log.i(LOG_TAG, "Not stopping LocatinoUpdateService since send offline status to backend failed");
                    }
                    break;
            }
            realm.close();
        }
    }

    public static boolean isServiceRunning() {
        Log.i(LOG_TAG, "isServiceRunning :" + isServiceRunning);
        return isServiceRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "onCreate()");

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)           //Needed for getting latest location using FusedLocationApi
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        handlerThread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = handlerThread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        LOCATIONUPDATEURL = getString(R.string.server_address) + getString(R.string.path_bikerLocUpdate);
        GOOFFLINEURL = getString(R.string.server_address) + getString(R.string.path_bikerGoOffline);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand()");
        connectGoogleApiClient();
        if ( intent != null && intent.getAction() != null && intent.getAction().equals(CHANGE_VISIBILITY_ACTION_INTENT)) {
            Log.i(LOG_TAG, "Action-CHANGE_VISIBILITY_ACTION_INTENT");
            //TODO -- send a handler message to send HTTP request to server notifying change of availability to NO.
            if (!CommonUtilities.getConnectivityStatus(this)) {
                Toast.makeText(getApplicationContext(), "Internet should be accessible", Toast.LENGTH_LONG).show();
                return  START_STICKY;
            } else {
                Message message = mServiceHandler.obtainMessage();
                message.what = HANDLEMSG_GO_OFFLINE;
                mServiceHandler.sendMessage(message);
            }

        } else {
            Log.i(LOG_TAG, "Action-NULL::Staring Foreground service");
            isServiceRunning = true;
            startForeground(NOTIFICATION_ID, constructNotificationHelper());
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG_TAG, "onConnected()");
        mFlagGooglePlayConnectionPresent = true;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "onConnectionSuspended of googleAPIClient. Trying to connect again");
        mFlagGooglePlayConnectionPresent = false;
        connectGoogleApiClient();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mFlagGooglePlayConnectionPresent = false;
        Log.i(LOG_TAG, "onConnectionFailed of googleAPIClient due to errorcode :" + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOG_TAG, "onLocationChanged with loc: (" + location.getLatitude() + ", " + location.getLongitude());
        SharedPrefHelper.setLatestLatLng(this, location.getLatitude(), location.getLongitude());

        if(mLastHeartBeatSent == 0L) {
            Log.i(LOG_TAG, "mLastHeartBeatSent set to current");
            mLastHeartBeatSent = System.currentTimeMillis();
        } else {
            if ((System.currentTimeMillis() - mLastHeartBeatSent) > BIKER_HEARTBEAT_INTERVAL_IN_MS) {
                //TODO: Sent heartbeat to Gservices to keep tcp connection alive with GCM servers
                //https://productforums.google.com/forum/#!msg/nexus/fslYqYrULto/lU2D3Qe1mugJ
                Log.i(LOG_TAG, "HeartBeat being sent");
                sendBroadcast(GTALK_HEART_BEAT_INTENT);
                sendBroadcast(MCS_MCS_HEARTBEAT_INTENT);
                mLastHeartBeatSent = System.currentTimeMillis();
            }
        }

        Message message = mServiceHandler.obtainMessage();
        message.obj = location;
        message.what = HANDLEMSG_UPDATELOCATION;
        mServiceHandler.sendMessage(message);
    }

    private void connectGoogleApiClient() {
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected() || !mFlagGooglePlayConnectionPresent) {
            Log.i(LOG_TAG, "Requesting connectClient again since it is not present()");
            mGoogleApiClient.connect();
        }
    }

    private Notification constructNotificationHelper() {
        Intent intentForFullNotif = new Intent(this, BikerMapActivity.class);
        //TaskStackBuilder provides a backward-compatible way to obey the correct conventions
        // around cross-task navigation on the device's version of the platform
        PendingIntent pendingIntentForFullNotif = TaskStackBuilder.create(this).addNextIntent(intentForFullNotif).getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentForGoInaactive = new Intent(this, LocationUpdationService.class);
        intentForGoInaactive.setAction(CHANGE_VISIBILITY_ACTION_INTENT);
        PendingIntent pendingIntentForGoInactive = PendingIntent.getService(this, 0, intentForGoInaactive, 0);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setColor(R.color.color_primary)
                .setTicker("You Are Active Now")
                .setContentTitle("You Are Active Now")
                .setContentText("Status:Available")
                .addAction(R.drawable.inactive_status_btn, "Turn Inactive", pendingIntentForGoInactive)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.biker_mapmarker))
                .setContentIntent(pendingIntentForFullNotif);

        return notifBuilder.build();
    }

    @Override
    public void onDestroy() { //Will not be called on force stops
        Log.i(LOG_TAG, "onDestroy()");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        //To avoid unusual case of BIKERAVAILABLE and Service not starting on App updations and service sudden killings
        isServiceRunning = false;
        mGoogleApiClient.disconnect();
        handlerThread.quit();
        super.onDestroy();
    }


    private boolean sendProto(byte[] bytesToBeSent, String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(10, TimeUnit.SECONDS);
        MediaType prototype = MediaType.parse("application/octet-stream; ");
        try {
            RequestBody requestBody = RequestBody.create(prototype, bytesToBeSent);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() == 200 && response.body().string().equals(Constants.ResponseStatusCode.SUCCESS)) {
                return true;
            }
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "HTTP Code is not 200. It is: " + response.code());
            Log.i(LOG_TAG, "HTTP POST Response status code is not '200' :" + response.code());
        }catch (IOException ioe) {
            Log.i(LOG_TAG, "IOexception by OKHTTP");
            ioe.printStackTrace();
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "While Sending Location Proto to Server");
            Crashlytics.logException(ioe);
        }
        catch (Exception e) {
            e.printStackTrace();
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "While Sending Location Proto to Server");
            Crashlytics.logException(e);
        }
        return false;

//        HttpClient httpclient = null;
//        try {
//            httpclient = new DefaultHttpClient();
//            HttpParams httpParams = httpclient.getParams();
//            int timeout = 10; //Unit in seconds
//            //Connection Timeout (http.connection.timeout) – the time to establish the connection with the remote host
//            httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout * 1000);
//            //Socket Timeout (http.socket.timeout) – the time waiting for data – after the connection was established; maximum time of inactivity between two data packets
//            httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout * 1000);
//
//            HttpPost httpPost = new HttpPost(url);
//            ByteArrayEntity byteArrayEntity = new ByteArrayEntity(bytesToBeSent);
//            byteArrayEntity.setContentType("application/x-protobuf");
//            httpPost.setEntity(byteArrayEntity);
//
//            HttpResponse response = httpclient.execute(httpPost);
//            int statuscode = response.getStatusLine().getStatusCode();
//            if ( statuscode == 200) {
//                String responeContent = EntityUtils.toString(response.getEntity());
//                if (responeContent.equals(Constants.ResponseStatusCode.SUCCESS)) return true;
//                Log.i(LOG_TAG, "Server error updating Biker Status/Location: " + responeContent);
//                return false;
//            } else {
//                Crashlytics.setString("WHERE", LOG_TAG);
//                Crashlytics.setString("MESSAGE", "HTTP Code is not 200. It is: " + statuscode);
//                Log.i(LOG_TAG, "HTTP POST Response status code is not '200' :" + statuscode);
//                return false;
//            }
//        }catch (Exception e) {
//            Crashlytics.setString("WHERE", LOG_TAG);
//            Crashlytics.setString("MESSAGE", "While Sending Location Proto to Server");
//            Crashlytics.logException(e);
//            Log.i(LOG_TAG, "Send Proto Exception :" + e.getMessage());
//            e.printStackTrace();
//            return false;
//        } finally {
//            if (httpclient != null)
//                httpclient.getConnectionManager().shutdown();
//        }

    }

}
