package org.rocketsingh.android.rocketsinghbiker.ui.history;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.squareup.wire.Wire;

import org.rocketsingh.android.rocketsinghbiker.R;
import org.rocketsingh.android.rocketsinghbiker.realmmodels.BikerDetail;
import org.rocketsingh.android.rocketsinghbiker.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghbiker.ui.navdrawer.RSDrawerActivitywithGoogleApi;
import org.rocketsingh.android.rocketsinghbiker.utilities.CommonUtilities;
import org.rocketsingh.android.rocketsinghbiker.utilities.Constants;
import org.rocketsingh.android.rocketsinghbiker.utilities.ItemClickSupportforRecycler;
import org.rocketsingh.android.rocketsinghbiker.utilities.RecyclerViewExtended;
import org.rocketsingh.android.rocketsinghbiker.utilities.SharedPrefHelper;
import org.rocketsingh.android.rocketsinghbiker.utilities.TaskFragment;

import java.io.IOException;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import mobileresponse.AllRequestResponse;

/**
 * Created by Divyu on 10/5/2015.
 */
public class HistoryActivity extends RSDrawerActivitywithGoogleApi implements TaskFragment.TaskCallbacks {
    //Note:Since we are using CardView as individual items of recyclerview, useing a itemdecoration doesnot make sense.
    RecyclerViewExtended mRecyclerView;
    Realm mRealm;
    MyRealmListener mRealmListener;
    RealmResults<Trip> tripRealmResults;
    BikerDetail mBiker;
    HistoryTripAdapter mAdapter;

    private TaskFragment mTaskFragment;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private static final String LOG_TAG = HistoryActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isGoogleApiClientSetupNeeded(false);
        super.onCreate(savedInstanceState);
        createMenuDrawer(R.layout.activity_history);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
        }
        mRecyclerView = (RecyclerViewExtended)findViewById(R.id.RecyclerView);
        TextView textView = (TextView) findViewById(R.id.emptyTextView);
        mRecyclerView.setEmptyView(textView);

        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        if (mTaskFragment == null) {
            mTaskFragment = new TaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRealm = Realm.getInstance(this);
        mRealmListener = new MyRealmListener();
        mRealm.addChangeListener(mRealmListener);
        tripRealmResults = mRealm.where(Trip.class).findAll();
        tripRealmResults.sort("timeId", RealmResults.SORT_ORDER_DESCENDING);
        mAdapter = new HistoryTripAdapter(this, tripRealmResults);
        mRecyclerView.setAdapter(mAdapter);

        mBiker = mRealm.where(BikerDetail.class).findFirst();
        if (mBiker == null) {
            Log.i(LOG_TAG, "Biker Profile not found in Realm");
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "This causes difficulty in HistorySync");
            Crashlytics.logException(new NullPointerException("Biker Detail not found in Realm"));
        }
        if (tripRealmResults.size() == 0) { //If no results, sync from server
            fetchHistoryAsync();
        }

        ItemClickSupportforRecycler.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupportforRecycler.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Toast.makeText(HistoryActivity.this, "Clicked " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }//End of onCreate()

    public void fetchHistoryAsync() {
        if (mTaskFragment.isTaskStarted()) return;
        Object postArgs[] = new Object[2];
        if (mBiker != null) {
            Log.i(LOG_TAG, "Fetching from BackEnd");
            String url = getString(R.string.server_address) + getString(R.string.path_bikerhistory); //http://192.168.1.9:8809/
            url = url + "/" + mBiker.getPhoneNo();
            postArgs[0] = url;
            mTaskFragment.startGetTaskProto(postArgs);
        }
    }

    @Override
    public void onProgressUpdate(int percent) { }

    @Override
    public void onCancelled(String reason) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "History Sync Failed. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPostExecute(Object result) {
        Wire wire = new Wire();
        try {
            AllRequestResponse allHistoryResponse = wire.parseFrom((byte[])result, AllRequestResponse.class);
            switch (allHistoryResponse.status) {
                case Constants.ResponseStatusCode.SUCCESS:
                    List<Trip> trips = CommonUtilities.ProtoToPOJO.convert(allHistoryResponse);
                    Log.i(LOG_TAG, "Trips returned: " + trips.size());
                    SharedPrefHelper.Realm_Trips.setInsertType(HistoryActivity.this, SharedPrefHelper.Realm_Trips.FULLHISTORYSYNC);
                    mRealm.beginTransaction();
                    mRealm.copyToRealmOrUpdate(trips);
                    mRealm.commitTransaction();
                    mAdapter.notifyDataSetChanged();
                    break;
                case Constants.ResponseStatusCode.FAILED:
                    Toast.makeText(getApplicationContext(), "Failed to Fetch Bikers", Toast.LENGTH_SHORT).show();
                    break;
            }
        }catch (IOException e) {
            //Send log to dev on this crash
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "While doing History SYNC");
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        catch (Exception e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "While doing History SYNC");
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    public class MyRealmListener implements RealmChangeListener {
        @Override
        public void onChange() {
            Log.i(LOG_TAG, "Realm OnChange detected");
            switch (SharedPrefHelper.Realm_Trips.getInsertType(HistoryActivity.this)) {
                case SharedPrefHelper.Realm_Trips.FULLHISTORYSYNC:
                    Log.i(LOG_TAG, "FULLTRIPSYNC. ResultsSize:" + tripRealmResults.size());
                    mAdapter.notifyDataSetChanged();
                    SharedPrefHelper.Realm_Trips.setInsertType(HistoryActivity.this, -1);
                    break;
                case SharedPrefHelper.Realm_Trips.NEWTRIP:
                    Log.i(LOG_TAG, "NEWTRIPSYNC");
                    LinearLayoutManager layoutManager = (LinearLayoutManager)mRecyclerView.getLayoutManager();
                    int position = 0;
                    mAdapter.notifyItemInserted(position);
                    if (position == 0) layoutManager.scrollToPosition(position);
                    SharedPrefHelper.Realm_Trips.setInsertType(HistoryActivity.this, -1);
                    break;
            }
        }
    }// End of RealmListener

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerView.setAdapter(null); //For unregistering dataobservers in our customerecyclerview 'RecyclerViewExtended'
        mRealm.removeChangeListener(mRealmListener);
        mRealm.close();
    }
}//End of whole Class.
