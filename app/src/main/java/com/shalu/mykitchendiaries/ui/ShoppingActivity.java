package com.shalu.mykitchendiaries.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.shalu.mykitchendiaries.R;
import com.shalu.mykitchendiaries.ShoppingListReminder;
import com.shalu.mykitchendiaries.provider.KitchenProvider;
import com.shalu.mykitchendiaries.widget.ShoppingListWidget;

import static com.shalu.mykitchendiaries.ui.NotifyMeFragment.PERMISSIONS_REQUEST_FINE_LOCATION;
import static com.shalu.mykitchendiaries.ui.NotifyMeFragment.refreshPlacesData;

public class ShoppingActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = ShoppingActivity.class.getSimpleName();
    private static final String SHOPPING_FRAGMENT = "shopping_fragment" ;
    private static final String NOTIFY_FRAGMENT = "notify_fragment";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private boolean isLandscape;
    Fragment shoppingFragment, notifyFragment;
    public static GoogleApiClient mClient;

    public static final String SHOPPING_LIST_INDEX = "s_index";
    public static int sposition;
    public static final String LOCATION_LIST_INDEX = "l_index";
    public static int lposition;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        isLandscape = getResources().getBoolean(R.bool.isLandscape);

        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

        if(!isLandscape) {

            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

            mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        }
        else {
            if(savedInstanceState != null) {
                shoppingFragment = getSupportFragmentManager().getFragment(savedInstanceState, SHOPPING_FRAGMENT);
                notifyFragment = getSupportFragmentManager().getFragment(savedInstanceState, NOTIFY_FRAGMENT);
                sposition = savedInstanceState.getInt(SHOPPING_LIST_INDEX);
            }
                if (shoppingFragment == null)
                    shoppingFragment = new ShoppingListFragment();
                else {
                    Fragment.SavedState savedState = getSupportFragmentManager().saveFragmentInstanceState(shoppingFragment);
                    shoppingFragment = new ShoppingListFragment();
                    shoppingFragment.setInitialSavedState(savedState);
                }
                if (notifyFragment == null)
                    notifyFragment = new NotifyMeFragment();
                else {
                Fragment.SavedState savedState = getSupportFragmentManager().saveFragmentInstanceState(notifyFragment);
                notifyFragment = new NotifyMeFragment();
                notifyFragment.setInitialSavedState(savedState);
            }

                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.shopping_list_container, shoppingFragment).commit();
                fragmentManager.beginTransaction().replace(R.id.notify_me_container, notifyFragment).commit();

        }
        ShoppingListReminder.scheduleReminder(this);
// Build up the LocationServices API client
        // Uses the addApi method to request the LocationServices API
        // Also uses enableAutoManage to automatically when to connect/suspend the client

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        refreshPlacesData(this);
        Log.i(TAG, "API Client Connection Successful!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "API Client Connection suspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "API Client Connection failed!");
    }

    public void onLocationPermissionClicked(View view) {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_FINE_LOCATION);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                if(shoppingFragment == null)
                    shoppingFragment = new ShoppingListFragment();
                return shoppingFragment;
            }
            else {
                if(notifyFragment == null)
                    notifyFragment = new NotifyMeFragment();
                return notifyFragment;
            }
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
           // return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0: {
                    shoppingFragment = (ShoppingListFragment) createdFragment;
                    break;
                }
                case 1: {
                    notifyFragment = (NotifyMeFragment) createdFragment;
                    break;
                }
            }
            return createdFragment;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, SHOPPING_FRAGMENT, shoppingFragment);
        getSupportFragmentManager().putFragment(outState, NOTIFY_FRAGMENT, notifyFragment);
    }

    public static void updateWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ShoppingListWidget.class));
        for (int appWidgetId : appWidgetIds) {
            ShoppingListWidget.updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}
