package org.rocketsingh.android.rocketsinghpillion.ui.navdrawer;

/**
 * Created by Divyu on 6/6/2015.
 */

import android.content.Context;

import org.rocketsingh.android.rocketsinghpillion.R;
import org.rocketsingh.android.rocketsinghpillion.ui.BikerMap.BikerMapActivity;
import org.rocketsingh.android.rocketsinghpillion.ui.Page2Activity;
import org.rocketsingh.android.rocketsinghpillion.ui.Page3Activity;
import org.rocketsingh.android.rocketsinghpillion.ui.TripActivity;
import org.rocketsingh.android.rocketsinghpillion.ui.history.HistoryActivity;
import org.rocketsingh.android.rocketsinghpillion.utilities.SharedPrefHelper;

import java.util.ArrayList;
/*
 * used by DrawerAdapter to maintain a list of items in the drawer
 */

public class DrawerItems {

    private final ArrayList<DrawerItem> mItems = new ArrayList<DrawerItem>();

    public DrawerItems(Context context) {
        super();
        mItems.clear();
        for(DrawerItemId iItem: DrawerItemId.values()) {
            addIfVisible(context, iItem);
        }
    }

    int size() {
        return mItems.size();
    }

    DrawerItem get(int position) {
        if (position < 0 || position >= mItems.size()) {
            return null;
        }
        return mItems.get(position);
    }

    private void addIfVisible(Context context, DrawerItemId id) {
        DrawerItem item = new DrawerItem(id);
        if (item.isVisible(context)) mItems.add(item);
    }

    boolean hasSelectedItem(Context context) {
        for (DrawerItem item: mItems) {
            if (item.isSelected(context)) {
                return true;
            }
        }
        return false;
    }

    /*
     *
     */
    enum DrawerItemId {
        PAGE1,
        PAGE2,
        PAGE3
    }

    /*
     *
     */
    static class DrawerItem {
        private final DrawerItemId mItemId;

        DrawerItem(DrawerItemId itemId) {
            mItemId = itemId;
        }

        DrawerItemId getDrawerItemId() {
            return mItemId;
        }


        int getTitleResId() {
            switch (mItemId) {
                case PAGE1 :
                    return R.string.title_activity_biker_map;
                case PAGE2:
                    return R.string.title_activity_trip;
                case PAGE3:
                    return R.string.title_activity_history;
                default :
                    return 0;
            }
        }

        int getIconResId() {
            switch (mItemId) {
                case PAGE1:
                    return R.drawable.navdrawer_bedsolid_logo;
                case PAGE2:
                    return R.drawable.navdrawer_dining_logo;
                case PAGE3:
                    return R.drawable.navdrawer_livingroom_logo;
                default :
                    return 0;
            }
        }

        boolean isSelected(Context context) {
            switch (mItemId) {
                case PAGE1:
                    return context instanceof BikerMapActivity;
                case PAGE2:
                    return context instanceof TripActivity;
                case PAGE3:
                    return (context instanceof HistoryActivity);
                default :
                    return false;
            }
        }

        boolean isVisible(Context context) {
            switch (mItemId) {
                case PAGE1:
                    if (SharedPrefHelper.isOnTrip(context)) return false;
                    else return true;
                case PAGE2:
                    if (!SharedPrefHelper.isOnTrip(context)) return false;
                    else return true;
                default ://Unless mentioned above, all items are visible by default
                    return true;
            }
        }
        /*
         * returns true if the item should have a divider beneath it
         */
        boolean hasDivider() {
            return (mItemId == DrawerItemId.PAGE2)||(mItemId == DrawerItemId.PAGE1);
        }
    }
}
