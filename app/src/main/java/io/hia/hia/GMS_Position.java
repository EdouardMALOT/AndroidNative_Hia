package io.hia.hia;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by edouard on 07/12/16.
 */

//GPS location using Google services, I hope it works better than standard android GPS location.
public class GMS_Position implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private Context         mContext;
    private GPS_Response    mreturnTask;
    private Location        mLastLocation;

    //////////////////////////////////////////////////
    //                                              //
    //               Constructor                    //
    //                                              //
    //////////////////////////////////////////////////
    public GMS_Position(Context context, GPS_Response returntask) {

        mContext = context;
        mreturnTask = returntask;

            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        mGoogleApiClient.connect();
        Log.d(MainActivity.LOG_TAG, "GoogleApiClient : Constructor");
    }

    //////////////////////////////////////////////////
    //                                              //
    //                      Stop                    //
    //                                              //
    //////////////////////////////////////////////////
    public void Stop() {
        mGoogleApiClient.disconnect();
        Log.d(MainActivity.LOG_TAG, "GoogleApiClient : stop");
    }

    //////////////////////////////////////////////////
    //                                              //
    //               GetLastLocation                //
    //                                              //
    //////////////////////////////////////////////////
    public Location GetLastLocation() {
        return mLastLocation;
    }

    //////////////////////////////////////////////////
    //                                              //
    //         Google services API callback         //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(MainActivity.LOG_TAG, "GoogleApiClient : onConnected");

        mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);       //High priority
            mLocationRequest.setInterval(0);                                          // Refresh every 100ms

            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                return;
            }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d(MainActivity.LOG_TAG, "GoogleApiClient location :" + location.toString());

        if (location != null) {
            mLastLocation = location;
            if(mreturnTask != null) {
                mreturnTask.GPS_position("OK", location.getLatitude(), location.getLongitude());
            }
        } else {
            if(mreturnTask != null) {
                mreturnTask.GPS_position("Error", 0.0, 0.0);
            }
        }

        Stop();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(MainActivity.LOG_TAG, "GoogleApiClient : connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(MainActivity.LOG_TAG, "GoogleApiClient : connection failed");
        mreturnTask.GPS_position("Error", 0.0, 0.0);
    }
}
