package org.rocketsingh.android.rocketsinghpillion.ui.history;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.rocketsingh.android.rocketsinghpillion.R;
import org.rocketsingh.android.rocketsinghpillion.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghpillion.utilities.CommonUtilities;
import org.rocketsingh.android.rocketsinghpillion.utilities.Constants;

import io.realm.RealmResults;


/**
 * Created by Divyu on 10/3/2015.
 */
public class HistoryTripAdapter extends RecyclerView.Adapter<HistoryTripAdapter.TripViewHolder> {

    private static final String LOG_TAG = HistoryTripAdapter.class.getSimpleName();
    private Context mContext;
    private RealmResults<Trip> items;

    public HistoryTripAdapter(Context context, RealmResults<Trip> items) {
        mContext = context;
        this.items = items;
    }

    @Override
    public HistoryTripAdapter.TripViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_trip_eachitem, viewGroup, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryTripAdapter.TripViewHolder viewHolder, int position) {
        Trip temp = items.get(position);
        viewHolder.starttime.setText("" + CommonUtilities.getFormattedMonthDay(temp.getTimeId()));
        switch (temp.getStatus()) {
            case Constants.TripStatusCode.TRIP_COMPLETED:
                viewHolder.status.setText("COMPLETED");break;
            case Constants.TripStatusCode.TRIP_CANCELLED:
                viewHolder.status.setText("CANCELLED");break;
            case Constants.TripStatusCode.TRIP_ACTIVE:
                viewHolder.status.setText("ACTIVE");break;
        }

        viewHolder.originlocality.setText("" + temp.getOrigin());
        viewHolder.triptime.setText("" + mContext.getString(R.string.format_time, temp.getTripTime() / 60000));
        viewHolder.tripcost.setText("" + mContext.getString(R.string.format_amount_INR, temp.getTripCost()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItemToDataSet(Trip item, int position) {
        items.add(position, item);
    }

    public static final class TripViewHolder extends RecyclerView.ViewHolder {
        public TextView starttime, status, originlocality, triptime, tripcost;
        public TripViewHolder(View itemView) {
            super(itemView);
            starttime = (TextView)itemView.findViewById(R.id.ongoingtrip_item_timesent);
            status = (TextView)itemView.findViewById(R.id.ongoingtrip_item_status);
            originlocality = (TextView)itemView.findViewById(R.id.ongoingtrip_item_originlocality);
            triptime = (TextView)itemView.findViewById(R.id.list_item_triptime);
            tripcost = (TextView)itemView.findViewById(R.id.list_item_tripcost);
        }
    }
}
