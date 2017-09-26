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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.wire.Wire;

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
import org.rocketsingh.android.rocketsinghbiker.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghbiker.ui.BikerMap.BikerMapActivity;
import org.rocketsingh.android.rocketsinghbiker.utilities.Constants;
import org.rocketsingh.android.rocketsinghbiker.utilities.SharedPrefHelper;

import java.util.ArrayList;
import java.util.List;

import biker.BikerLocation;
import mobilerequest.RequestData;
import request.TripData;
import mobileresponse.PaymentDetailResponse;
import io.realm.Realm;
import io.realm.RealmChangeListener;

public class TripUpdationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected GoogleApiClient mGoogleApiClient;
    protected boolean mFlagResolvingError = false;
    protected boolean mFlagGooglePlayConnectionPresent = false;
    private boolean mRequestingLocationUpdates; //Used as flag for not sending LocationRequests twice.
    private LocationRequest mLocationRequest;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private HandlerThread handlerThread;

    private static final String LOG_TAG = TripUpdationService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 100;
    public static final String END_TRIP_ACTION_INTENT = "END_TRIP_ACTION_INTENT";
    private static final int HANDLEMSG_UPDATELOCATION = 1;
    private static final int HANDLEMSG_ENDTRIP = 2;

    private String LOCATIONUPDATEURL;
    private String ENDTRIPURL;
    Realm mRealm;
    MyRealmListener mRealmListener;
    Trip mTrip;

    //Flag to start Service in BikerMap Activity if not started yet.
    private static boolean isServiceRunning;

    private static final long  UPDATE_INTERVAL_IN_MS = 8000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MS = 7000;
    //Only variable referenced to know how frequently bikerlocation has to be updated.
    //Set to TRIPSTARTED_LOCUPDATEINTERVALTIME once trip has started
    private static long LOCUPDATEDINTERVALTIME = FASTEST_UPDATE_INTERVAL_IN_MS;
    //Used for limit on how frequently bikerlocation should be updated once trip has started.
    private static final long TRIPSTARTED_LOCUPDATEINTERVALTIME = 20 * 1000; //20 secs
    private long mLastLocSentTime; //last time Location was sent to backend
    private long mTripStartedTime; //time at which trip has started from customer place(in millisec)
    private Location mLastLocation; //Used for calculating distance
    private double mTripDistance; //Distance calculated in meters
    //To access currentrip's timeId in Handler Thread (RealmObjects cannot be used in different threads)
    private long mTripTimeID;
    private List<Double> mOnWayLatitudes = new ArrayList<>();
    private List<Double> mOnWayLongitudes = new ArrayList<>();

    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLEMSG_UPDATELOCATION: {
                    Location location = (Location) msg.obj;
                    Realm realm = Realm.getInstance(TripUpdationService.this);
                    BikerDetail bikerDetail = realm.where(BikerDetail.class).findFirst();
                    Log.i(LOG_TAG, "Handler executing message for location:(" + location.getLatitude() + ", " + location.getLongitude() + ")");
                    synchronized (this) {
                        try {
                            //Sending biker location data here through a HTTP POST
                            BikerLocation bikerLocation = new BikerLocation.Builder()
                                    .selfie_url("")
                                    .first_name(bikerDetail.getName())
                                    .last_name("")
                                    .bike_license_plate(bikerDetail.getBikeLicensePlate())
                                    .phone(bikerDetail.getPhoneNo())
                                    .alternate_phone("")
                                    .biker_type(bikerDetail.getBikerType())
                                    .current_lat(location.getLatitude())
                                    .current_long(location.getLongitude())
                                    .date_created(bikerDetail.getDateCreated())  //date at which the biker account is created
                                    .biker_id(Long.valueOf(bikerDetail.getPhoneNo()))
                                    .build();

                            sendProto(bikerLocation.toByteArray(), HANDLEMSG_UPDATELOCATION, LOCATIONUPDATEURL, null, null);
                        } catch (Exception e) {
                            Crashlytics.setString("WHERE", LOG_TAG);
                            Crashlytics.setString("MESSAGE", "While Updating Location in Bgnd Handler");
                            Crashlytics.logException(e);
                            e.printStackTrace();
                        }
                    }
                    realm.close();
                }
                break;
                case HANDLEMSG_ENDTRIP: {
                    //Send Tripdata and end status to Server.
                    Realm realm = Realm.getInstance(TripUpdationService.this);
                    Trip trip =realm.where(Trip.class).equalTo("timeId", mTripTimeID).findFirst();
                    long tripTime = System.currentTimeMillis() - mTripStartedTime;

                    TripData tripData = new TripData.Builder()
                            .trip_distance(mTripDistance)
                            .trip_time(tripTime)
                            .start_latitude(trip.getStartLatitude())
                            .start_longitude(trip.getStartLongitude())
                            .end_latitude(trip.getEndLatitude())
                            .end_longitude(trip.getEndLongitude())
                            .on_way_latitude(mOnWayLatitudes)
                            .on_way_longitude(mOnWayLongitudes)
                            .build();

                    RequestData requestData = new RequestData.Builder()
                            .entity_id(trip.getEntityId())
                            .entity_type(Integer.parseInt(trip.getEntityType()))
                            .time_id(String.valueOf(trip.getTimeId()))
                            .request_status_flag(Constants.TripStatusCode.TRIP_COMPLETED)
                            .tripData(tripData)
                            .build();
                    if (sendProto(requestData.toByteArray(), HANDLEMSG_ENDTRIP, ENDTRIPURL, trip, realm)) {
                        realm.beginTransaction();
                        trip.setTripTime(tripTime);
                        trip.setTripDistance(mTripDistance);
                        Log.i(LOG_TAG, "TotalTripTime: " + trip.getTripTime());
                        Log.i(LOG_TAG, "TotalTripDistance: " + trip.getTripDistance());
                        //Note:: Tripcost and change of trip status to 'COMPLETED' is done once server POST is successful
                        realm.commitTransaction();
                        //Send a broadcast to BikerMapActivity about the success of the trip
                        Intent intent = new Intent(BikerMapActivity.NOTIFYENDTRIP_INTENTACTION);
                        intent.putExtra(BikerMapActivity.intentextrakey_tripendstatus, true);
                        intent.putExtra(BikerMapActivity.intentextrakey_triptimeid, mTripTimeID);
                        LocalBroadcastManager.getInstance(TripUpdationService.this).sendBroadcast(intent);
                        //Stop TripUpdationService
                        stopForeground(true);
                        stopSelf();
                        realm.close();
                    }
                }
                break;
            }
        } //End of handleMessage()
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
        ENDTRIPURL = getString(R.string.server_address) + getString(R.string.path_endtrip);
        mRealm = Realm.getInstance(this);
        mRealmListener = new MyRealmListener();
        mRealm.addChangeListener(mRealmListener);
        mTrip = mRealm.where(Trip.class).equalTo("status", Constants.TripStatusCode.TRIP_ACTIVE)
                .or().equalTo("status", Constants.TripStatusCode.TRIP_STARTED)
                .findFirst();
        mTripTimeID = mTrip.getTimeId();
        Log.i(LOG_TAG, "Assigning timeid to :" + mTripTimeID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand()");
        connectGoogleApiClient();
        if (intent != null && intent.getAction() != null && intent.getAction().equals(END_TRIP_ACTION_INTENT)) {
            Log.i(LOG_TAG, "Action-END_TRIP_ACTION_INTENT");
            //Sending a handler message to POST Tripdata to server
            Message message = mServiceHandler.obtainMessage();
            message.what = HANDLEMSG_ENDTRIP;
            mServiceHandler.sendMessage(message);
            //If success, trip status is updated in local db and service is stopped

        } else {
            Log.i(LOG_TAG, "Action-NULL::Staring Foreground service");
            Log.i(LOG_TAG, "Timecount :" + (System.currentTimeMillis() - mTripStartedTime) + ", midwaylistsize :" + mOnWayLongitudes.size());
            isServiceRunning = true;
            startForeground(NOTIFICATION_ID, constructNotificationHelper());
        }
        return START_STICKY;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOG_TAG, "onLocationChanged with loc: (" + location.getLatitude() + ", " + location.getLongitude());
        mOnWayLatitudes.add(location.getLatitude());  mOnWayLongitudes.add(location.getLongitude());
        long now = System.currentTimeMillis();
        if ((now - mLastLocSentTime) >= LOCUPDATEDINTERVALTIME) {
            Message message = mServiceHandler.obtainMessage();
            message.obj = location;
            message.what = HANDLEMSG_UPDATELOCATION;
            mServiceHandler.sendMessage(message);
            mLastLocSentTime = System.currentTimeMillis();
        }
        //If Trip has started, start summing up distance. :)
        if (mTrip.getStatus().equals(Constants.TripStatusCode.TRIP_STARTED)) {
            Log.i(LOG_TAG, "TripStarted- Distance being calculated");
            //TODO: Have to add a reasonable traffic validator and replace below logic
            // if (location.getSpeed() <= 1.0f) return;  //Assuming in traffic
            if (mLastLocation == null) {
                Log.w(LOG_TAG, "mLastLocation is NULL");
                mLastLocation = SharedPrefHelper.getLastLocation(this);
            }
            mTripDistance += location.distanceTo(mLastLocation);
        }
        mLastLocation = location;
        SharedPrefHelper.setLatestLatLng(this, location.getLatitude(), location.getLongitude());
    }

    private class MyRealmListener implements RealmChangeListener {
        @Override
        public void onChange() {
            if (SharedPrefHelper.Realm_Trips.getInsertType(TripUpdationService.this)
                    == SharedPrefHelper.Realm_Trips.TRIP_STARTED) {
                Log.i(LOG_TAG, "Timings re-configured");
                mTripStartedTime = System.currentTimeMillis();
                LOCUPDATEDINTERVALTIME = TRIPSTARTED_LOCUPDATEINTERVALTIME;
                SharedPrefHelper.Realm_Trips.setInsertType(TripUpdationService.this, -1);
            }
        }
    }

    private Notification constructNotificationHelper() {
        Intent intentForFullNotif = new Intent(this, BikerMapActivity.class);
        //TaskStackBuilder provides a backward-compatible way to obey the correct conventions
        // around cross-task navigation on the device's version of the platform
        PendingIntent pendingIntentForFullNotif = TaskStackBuilder.create(this).addNextIntent(intentForFullNotif).getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        Intent intentForGoInaactive = new Intent(this, TripUpdationService.class);
//        intentForGoInaactive.setAction(END_TRIP_ACTION_INTENT);
//        PendingIntent pendingIntentForGoInactive = PendingIntent.getService(this, 0, intentForGoInaactive, 0);
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setColor(R.color.color_primary)
                .setTicker("You Are On Trip")
                .setContentTitle("You Are on Trip")
                .setContentText("Status:OnTrip")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.biker_mapmarker))
                .setContentIntent(pendingIntentForFullNotif);

        return notifBuilder.build();
    }

    public static boolean isServiceRunning() {
        return isServiceRunning;
    }

    private void connectGoogleApiClient() {
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected() || !mFlagGooglePlayConnectionPresent) {
            Log.i(LOG_TAG, "Requesting connectClient again since it is not present()");
            mGoogleApiClient.connect();
        }
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
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "onDestroy()");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        handlerThread.quit();
        isServiceRunning = false;
        mRealm.removeChangeListener(mRealmListener);
        mRealm.close();
        super.onDestroy();
    }

    //@Args: trip, realm could be null. Useful only for updating trip once it is ended in "HANDLEMSG_ENDTRIP" flow
    private boolean sendProto(byte[] bytesToBeSent, int type, String url, Trip trip, Realm realm) {
        HttpClient httpclient = null;
        try {
            httpclient = new DefaultHttpClient();
            HttpParams httpParams = httpclient.getParams();
            int timeout = 10; //Unit in seconds
            //Connection Timeout (http.connection.timeout) – the time to establish the connection with the remote host
            httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout * 1000);
            //Socket Timeout (http.socket.timeout) – the time waiting for data – after the connection was established; maximum time of inactivity between two data packets
            httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout * 1000);

            HttpPost httpPost = new HttpPost(url);
            ByteArrayEntity byteArrayEntity = new ByteArrayEntity(bytesToBeSent);
            byteArrayEntity.setContentType("application/octet-stream");
            httpPost.setEntity(byteArrayEntity);

            HttpResponse response = httpclient.execute(httpPost);
            int statuscode = response.getStatusLine().getStatusCode();
            if (statuscode == 200) {
                if (type == HANDLEMSG_UPDATELOCATION) {
                    String responeContent = EntityUtils.toString(response.getEntity());
                    if (responeContent.equals(Constants.ResponseStatusCode.SUCCESS)) return true;
                    Log.i(LOG_TAG, "Server error updating Biker Status/Location: " + responeContent);
                    return false;
                } else if (type == HANDLEMSG_ENDTRIP) {
                    Wire wire = new Wire();
                    PaymentDetailResponse paymentDetailResponse =
                            wire.parseFrom(EntityUtils.toByteArray(response.getEntity()), PaymentDetailResponse.class);
                    if (paymentDetailResponse.status.equals(Constants.ResponseStatusCode.SUCCESS)) {
                        realm.beginTransaction();
                        trip.setStatus(Constants.TripStatusCode.TRIP_COMPLETED);
                        trip.setTripCost(paymentDetailResponse.paymentDetails.trip_cost);
                        realm.commitTransaction();
                        return true;
                    }
                }
            } else {
                Log.i(LOG_TAG, "HTTP POST Response status code is not '200' :" + statuscode);
            }
        }catch (Exception e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "While Sending Proto to Server for type : " + type);
            Crashlytics.logException(e);
            Log.i(LOG_TAG, "Exception :" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (httpclient != null)
                httpclient.getConnectionManager().shutdown();
        }
        if (type == HANDLEMSG_ENDTRIP) {
            //Send a broadcast to BikerMapActivity about the failure of sending trip details to server
            Intent intent = new Intent(BikerMapActivity.NOTIFYENDTRIP_INTENTACTION);
            intent.putExtra(BikerMapActivity.intentextrakey_tripendstatus, true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        return false;
    }

}
