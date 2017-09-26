package org.rocketsingh.android.rocketsinghbiker.ui.navdrawer;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.rocketsingh.android.rocketsinghbiker.R;
import org.rocketsingh.android.rocketsinghbiker.ui.BikerMap.BikerMapActivity;
import org.rocketsingh.android.rocketsinghbiker.ui.Page3Activity;
import org.rocketsingh.android.rocketsinghbiker.ui.history.HistoryActivity;
import org.rocketsingh.android.rocketsinghbiker.ui.navdrawer.DrawerItems.DrawerItem;
import org.rocketsingh.android.rocketsinghbiker.utilities.DisplayUtils;

/**
 * Created by Divyu on 6/6/2015.
 */
public class RSDrawerActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mUserLearnedDrawer;
    private ListView mDrawerListView;
    private DrawerAdapter mDrawerAdapter;
    //If the Activity should be finished after an item in nav drawer is selected which calls another activity.
    private boolean mShouldFinish;

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final int OPENED_FROM_DRAWER_DELAY = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (isStaticMenuDrawer()) {
            setContentView(R.layout.activity_drawer_static);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.color_primary_dark));
            }
        } else {
            setContentView(R.layout.activity_drawer);
        }

        setSupportActionBar(getToolbar());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDrawerToggle != null) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            mDrawerToggle.syncState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mShouldFinish) {
            overridePendingTransition(0, 0);
            finish();
        }
    }

    /**
     * Create menu drawer ListView and listeners
     */
    private void initMenuDrawer() {
        // locate the drawer layout - note that it will not exist on landscape tablets
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            int drawerWidth =
                    isStaticMenuDrawer() ?
                            getResources().getDimensionPixelSize(R.dimen.drawer_width_static) :
                            DisplayUtils.getOptimalDrawerWidth(this);
            ViewGroup leftDrawer = (ViewGroup) mDrawerLayout.findViewById(R.id.capture_insets_frame_layout);
            leftDrawer.getLayoutParams().width = drawerWidth;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.color_primary_dark));
            }
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        //Tieing together the functionality of DrawerLayout and the framework ActionBar to implement the recommended design for navigation drawers.
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,
                    mDrawerLayout,
                    getToolbar(),
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
            ) {
                public void onDrawerClosed(View view) {
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    if (!mUserLearnedDrawer) {
                        // The user manually opened the drawer; store this flag to prevent auto-showing
                        // the navigation drawer automatically in the future.
                        mUserLearnedDrawer = true;
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(RSDrawerActivity.this);
                        sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                    }

                    invalidateOptionsMenu();
                }
            };

            if (!mUserLearnedDrawer) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }

            //Setting the functionality where click on the indicator drawable set below, triggers navdrawer.
            mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }

        mDrawerAdapter = new DrawerAdapter(this);
        mDrawerListView = (ListView) findViewById(R.id.drawer_list);
        mDrawerListView.setAdapter(mDrawerAdapter);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerItem drawerItem = (DrawerItem)mDrawerAdapter.getItem(position);
                drawerItemSelected(drawerItem);
            }
        });
    }

        /**
         * Create a menu drawer and attach it to the activity.
         * @param contentViewId of the main content for the activity.
         */
    protected void createMenuDrawer(int contentViewId) {
        ViewGroup container = (ViewGroup) findViewById(R.id.activity_container);
        //Adds the actual activity's content to the layout.
        container.addView(getLayoutInflater().inflate(contentViewId, null));

        initMenuDrawer();
    }

    /**
     * called when user selects an item from the drawer
     */
    private void drawerItemSelected(DrawerItem item) {
        // do nothing if item is already selected
        if (item == null || item.isSelected(this)) {
            closeDrawer();
            return;
        }

        final Intent intent;
        switch (item.getDrawerItemId()) {
            case PAGE1:
                mShouldFinish = true;
                intent = new Intent(this, BikerMapActivity.class);
                break;
            case PAGE2:
                mShouldFinish = true;
                intent = new Intent(this, HistoryActivity.class);
                break;
            case PAGE3:
                mShouldFinish = true;
                intent = new Intent(this, Page3Activity.class);
                break;
            default :
                mShouldFinish = false;
                intent = null;
                break;
        }

        if (intent == null) {
            Toast.makeText(this, "Unable to perform this action", Toast.LENGTH_LONG).show();
            return;
        }

        if (mShouldFinish) {
            // set the ActionBar title to that of the incoming activity
            if (getSupportActionBar() != null) {
                int titleResId = item.getTitleResId();
                if (titleResId != 0) {
                    getSupportActionBar().setTitle(getString(titleResId));
                } else {
                    getSupportActionBar().setTitle(null);
                }
            }

            // close the drawer and fade out the activity container so current activity appears to be going away
            closeDrawer();
            hideActivityView();

            // start the new activity after a brief delay to give drawer time to close
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            }, OPENED_FROM_DRAWER_DELAY);
        } else {
            // current activity isn't being finished, so just start the new activity
            closeDrawer();
            startActivity(intent);
        }
    }

    public boolean isStaticMenuDrawer() {
        return DisplayUtils.isLandscape(this)
                && DisplayUtils.isXLarge(this);
    }

    protected Toolbar getToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        }
        return mToolbar;
    }

    void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    /*
 * fade out the view containing the current drawer activity
 */
    private void hideActivityView() {
        // activity_container is the parent view which contains the toolbar (first child) and
        // the activity itself (second child)
        ViewGroup container = (ViewGroup) findViewById(R.id.activity_container);
        if (container == null || container.getChildCount() < 2) {
            return;
        }
        final View activityView = container.getChildAt(1);
        if (activityView == null || activityView.getVisibility() != View.VISIBLE) {
            return;
        }
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(activityView, View.ALPHA, 1.0f, 0.0f);
        fadeOut.setDuration(OPENED_FROM_DRAWER_DELAY);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.start();
    }


}

