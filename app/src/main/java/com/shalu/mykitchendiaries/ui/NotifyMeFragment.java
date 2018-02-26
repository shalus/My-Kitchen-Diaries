package com.shalu.mykitchendiaries.ui;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.shalu.mykitchendiaries.Geofencing;
import com.shalu.mykitchendiaries.R;
import com.shalu.mykitchendiaries.provider.KitchenProvider;
import com.shalu.mykitchendiaries.provider.LocationContract;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.shalu.mykitchendiaries.ui.ShoppingActivity.LOCATION_LIST_INDEX;
import static com.shalu.mykitchendiaries.ui.ShoppingActivity.lposition;

public class NotifyMeFragment extends Fragment {

    public static final String TAG = ShoppingActivity.class.getSimpleName();
    @BindView(R.id.places_list_recycler_view) RecyclerView mPlaceRecyclerView;
    static PlaceListAdapter placeListAdapter;
    static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static boolean mIsEnabled;
    private static GoogleApiClient mClient;
    private static Geofencing mGeofencing;
    LinearLayoutManager mLayoutManager;
    private Unbinder unbinder;

    View rootView;
    public NotifyMeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mClient = ShoppingActivity.mClient;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         mGeofencing = new Geofencing(getContext(), mClient);
        rootView = inflater.inflate(R.layout.fragment_notify_me, container, false);
        unbinder = ButterKnife.bind(this, rootView);
//        mPlaceRecyclerView = (RecyclerView) rootView.findViewById(R.id.places_list_recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        mPlaceRecyclerView.setLayoutManager(mLayoutManager);
        placeListAdapter = new PlaceListAdapter(getContext(), null);
        mPlaceRecyclerView.setAdapter(placeListAdapter);


        // Initialize the switch state and Handle enable/disable switch change
        Switch onOffSwitch = (Switch) rootView.findViewById(R.id.enable_switch);
        mIsEnabled = getActivity().getPreferences(MODE_PRIVATE).getBoolean(getString(R.string.setting_enabled), false);
        onOffSwitch.setChecked(mIsEnabled);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getActivity().getPreferences(MODE_PRIVATE).edit();
                editor.putBoolean(getString(R.string.setting_enabled), isChecked);
                mIsEnabled = isChecked;
                editor.commit();
                if (isChecked) mGeofencing.registerAllGeofences();
                else mGeofencing.unRegisterAllGeofences();
            }

        });
        Button addPlace = (Button) rootView.findViewById(R.id.add_place_button);
        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    // Start a new Activity for the Place Picker API, this will trigger {@code #onActivityResult}
                    // when a place is selected or with the user cancels.
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    Intent i = builder.build(getActivity());
                    startActivityForResult(i, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
                } catch (Exception e) {
                    Log.e(TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
                }
            }
        });
        return rootView;

    }
    public static void refreshPlacesData(Context context) {
        Uri uri = KitchenProvider.Location.CONTENT_URI;
        Cursor data = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                null);

        if (data == null || data.getCount() == 0) return;
        List<String> guids = new ArrayList<String>();
        while (data.moveToNext()) {
            guids.add(data.getString(data.getColumnIndex(LocationContract.PLACES_ID)));
        }
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient,
                guids.toArray(new String[guids.size()]));
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                placeListAdapter.swapPlaces(places);
                mGeofencing.updateGeofencesList(places);
                if (mIsEnabled) mGeofencing.registerAllGeofences();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(getContext(), data);
            if (place == null) {
                Log.i(TAG, "No place selected");
                return;
            }

            // Extract the place information from the API
            String placeName = place.getName().toString();
            String placeAddress = place.getAddress().toString();
            String placeID = place.getId();

            // Insert a new place into DB
            ContentValues contentValues = new ContentValues();
            contentValues.put(LocationContract.PLACES_ID, placeID);
            getContext().getContentResolver().insert(KitchenProvider.Location.CONTENT_URI, contentValues);

            // Get live data information
            refreshPlacesData(getContext());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            int position = savedInstanceState.getInt(LOCATION_LIST_INDEX);
            if(position != RecyclerView.NO_POSITION && position != 0)
                lposition = position;
        }
        mPlaceRecyclerView.smoothScrollToPosition(lposition);
    }

    @Override
    public void onResume() {

            super.onResume();
            // Initialize location permissions checkbox
            CheckBox locationPermissions = (CheckBox) rootView.findViewById(R.id.location_permission_checkbox);
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationPermissions.setChecked(false);
            } else {
                locationPermissions.setChecked(true);
                locationPermissions.setEnabled(false);
            }

        }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mLayoutManager!=null) {
                outState.putInt(LOCATION_LIST_INDEX, mLayoutManager.findFirstCompletelyVisibleItemPosition());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
