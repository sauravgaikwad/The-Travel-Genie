package com.example.jigya.travelgenie7;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks ,GoogleApiClient.OnConnectionFailedListener,  com.google.android.gms.location.LocationListener {
    private static final long INTERVAL=1000*2;
    private static final long FASTEST_INTERVAL=1000;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation,lStart,lEnd;
    static double distance=0;
    private final IBinder mBinder=new LocalBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        mGoogleApiClient=new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
        return mBinder;
    }
    public void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try
        {
            // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, (com.google.android.gms.location.LocationListener) this);
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }

        }
        catch (SecurityException e)
        {
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        DSP.progressDialog.dismiss();
        mCurrentLocation=location;
        if(lStart==null)
        {
            lStart=lEnd=mCurrentLocation;
        }
        else
            lEnd=mCurrentLocation;
        updateUI();
    }
    public void updateUI()
    {
        if(DSP.p==0)
        {
            distance=distance+(lStart.distanceTo(lEnd)/1000.00);
            DSP.endTime=System.currentTimeMillis();
            long diff=DSP.endTime-DSP.startTime;
            diff= TimeUnit.MILLISECONDS.toMinutes(diff);
            String s="Total Time " + diff + "Minutes";
            DSP.time.setText(s);
            DSP.distance.setText(new DecimalFormat("#.###").format(distance));
            lStart=lEnd;
        }

    }
    @Override
    public boolean onUnbind(Intent intent)
    {
        stopLocationUpdates();
        if(mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart=lEnd=null;
        distance=0;

        return super.onUnbind(intent);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        distance=0;
    }



    public class LocalBinder extends Binder {
        public LocationService getService()
        {
            return LocationService.this;
        }
    }

}


