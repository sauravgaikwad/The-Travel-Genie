package com.example.jigya.travelgenie7;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CreateGeofence extends AppCompatActivity

        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener ,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        ResultCallback<Status> {

    private static final String TAG=CreateGeofence.class.getSimpleName();



    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private Marker currentLocationMarker;
    private TextView textLat,textLong;
    private MapFragment mapFragment;
    private LocationRequest locationrequest;
    private static final String NOTIFICATION_MSG="NOTIFICATION_MSG";

    public static Intent makeNotificationIntent(Context context, String msg)
    {
        Intent intent=new Intent(context,CreateGeofence.class);
        intent.putExtra(NOTIFICATION_MSG,msg);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_geofence);
        textLat=(TextView)findViewById(R.id.lat);
        textLong=(TextView)findViewById(R.id.lon);

        initGMaps();
        createGoogleApi();


    }
    private void createGoogleApi()
    {
        Log.d(TAG,"createGoogleApi()");
        if(googleApiClient==null)
        {
            googleApiClient =new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();


        }
    }

    protected void onStart()
    {
        super.onStart();;
        googleApiClient.connect();
    }
    protected void onStop()
    {
        super.onStop();
        googleApiClient.disconnect();
    }
    public boolean onCreateOptionsMenu(Menu menu)
    {

        Toast.makeText(this,"Displaying menu",Toast.LENGTH_LONG).show();
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.geofence:
            {
                startGeofence();
                return true;
            }
            case R.id.clear: {
                clearGeofence();
                return true;
            }

        }
        return super.onOptionsItemSelected(item);

    }
    private final int REQ_PERMISSION=999;

    private boolean checkPermission()
    {
        Log.d(TAG,"checkPermission()");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)

            Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_SHORT).show();

        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);


    }
    private void askPermission()
    {
        Log.d(TAG,"askPermission()");
        ActivityCompat.requestPermissions(
                this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQ_PERMISSION);
    }

    public void onRequestPermissionResult(int requestCode,String [] permissions,int [] grantResults)
    {
        Log.d(TAG,"onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode)
        {
            case REQ_PERMISSION:
            {
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    getLastKnownLocation();
                }
                else
                {
                    permissionDenied();
                }
                break;

            }

        }

    }

    private void permissionDenied() {
        Log.w(TAG,"permissionDenied()");
        Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_SHORT).show();


    }
    private void initGMaps()
    {
        mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG,"onMapReady()");
        map=googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);


        map = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            map.setMyLocationEnabled(true);


        }




    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG,"onMapClick("+latLng+")");
        markerForGeofence(latLng);



    }


    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();
    }



    private LocationRequest locationRequest;
    private final int UPDATE_INTERVAL=1000;
    private final int FASTEST_INTERVAL=900;

    private void startLocationUpdates()
    {
        Log.i(TAG,"startLocationUpdates()");
        locationRequest=LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        if(checkPermission())
        {
            //LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
        }



    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d(TAG,"onLocationChanged ["+location+"]");
        lastLocation=location;
        if(currentLocationMarker!=null)
        {
            currentLocationMarker.remove();
        }
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationMarker=map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomBy(10));
        if(googleApiClient!=null)
        {
            //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, (com.google.android.gms.location.LocationListener) this);
        }


        writeActualLocation(location);




    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(TAG,"onConnected()");

        getLastKnownLocation();
        recoverGeofenceMarker();
        locationrequest = new LocationRequest();
        locationrequest.setInterval(1000);
        locationrequest.setFastestInterval(1000);
        locationrequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
        {}

        // LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationrequest, (com.google.android.gms.location.LocationListener) this);



    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"onConnectionSuspended()");



    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d(TAG,"onConnectionFailed()");


    }



    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG,"onMarkerClickListener: "+marker.getPosition());
        return false;
    }





    private void getLastKnownLocation()
    {
        Log.d(TAG,"getLastKnownLocation()");
        if(checkPermission())
        {
            lastLocation=LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if(lastLocation!=null)
            {
                Log.i(TAG,"lastKnownLocation. "+"Long: "+lastLocation.getLongitude()+" | Lat: "+lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();

            }
            else
            {
                Log.w(TAG,"No Location Retrived Yet");
                startLocationUpdates();

            }
        }else
        {
            askPermission();
        }

    }

    private void writeActualLocation(Location location)
    {
        textLat.setText("Lat: "+location.getLatitude());
        textLong.setText("Long: "+location.getLongitude());

        markerLocation(new LatLng(location.getLatitude(),location.getLongitude()));

    }
    private void writeLastLocation()
    {
        writeActualLocation(lastLocation);
    }
    private Marker locationMarker;
    private void markerLocation(LatLng latLng)
    {
        Log.i(TAG,"markerLocation("+latLng+")");
        String title=latLng.latitude+" , "+latLng.longitude;
        MarkerOptions markerOptions=new MarkerOptions()
                .position(latLng)
                .title(title);
        if(map!=null)
        {
            if(locationMarker!=null)
            {
                locationMarker.remove();
            }
            locationMarker=map.addMarker(markerOptions);
            float zoom=14f;
            CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngZoom(latLng,zoom);
            map.animateCamera(cameraUpdate);
        }


    }
    private Marker geofenceMarker;
    private void markerForGeofence(LatLng latLng)
    {
        Log.i(TAG,"markerForGeofence("+latLng+")");
        String title=latLng.latitude+" , "+latLng.longitude;
        MarkerOptions markerOptions=new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if(map!=null)
        {
            if(geofenceMarker!=null)
            {
                geofenceMarker.remove();
            }
            geofenceMarker=map.addMarker(markerOptions);
        }
    }
    private void startGeofence()
    {
        Log.i(TAG,"startGeofence()");
        if(geofenceMarker!=null)
        {
            Geofence geofence=createGeofence(geofenceMarker.getPosition(),GEOFENCE_RADIUS);
            GeofencingRequest geofencingRequest=createGeofenceRequest(geofence);
            addGeofence(geofencingRequest);

        }
        else
        {
            Log.i(TAG,"Geofence Marker Is NULL");

        }
    }
    private static final long GEOFENCE_DURATION=60*60*1000;
    private static final String GEOFENCE_REQ_ID="My Geofence";
    private static final float GEOFENCE_RADIUS=50.0f;


    private Geofence createGeofence(LatLng latLng,float radius)
    {
        Log.d(TAG,"createGeofence");
        return  new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude,latLng.longitude,radius)
                .setExpirationDuration(GEOFENCE_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

    }

    private GeofencingRequest createGeofenceRequest(Geofence geofence)
    {
        Log.d(TAG,"createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

    }

    private PendingIntent geofencePendingIntent;
    private final int GEOFENCE_REQ_CODE=0;
    private PendingIntent createGeofencePendingIntent()
    {
        Log.d(TAG,"createGeofencePendingIntent");
        if(geofencePendingIntent!=null)
        {
            return geofencePendingIntent;
        }
        Intent intent=new Intent(this,GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this,GEOFENCE_REQ_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT);

    }

    private void addGeofence(GeofencingRequest request)
    {
        Log.d(TAG,"Add Geofence");

        if(checkPermission())
        {
            LocationServices.GeofencingApi.addGeofences(googleApiClient,request,createGeofencePendingIntent()).setResultCallback(this);

        }
    }




    @Override
    public void onResult(@NonNull Status status) {

        Log.i(TAG,"onResult: "+status);
        if(status.isSuccess())
        {
            saveGeofence();
            drawGeofence();

        }
        else
        {

        }

    }



    private Circle geofenceLimits;
    private void drawGeofence()
    {
        Log.d(TAG,"drawGeofence()");
        if(geofenceLimits!=null)
        {
            geofenceLimits.remove();
        }
        CircleOptions circleOptions=new CircleOptions()
                .center(geofenceMarker.getPosition())
                .strokeColor(Color.argb(50,70,70,70))
                .fillColor(Color.argb(100,150,150,150))
                .radius(GEOFENCE_RADIUS);
        geofenceLimits=map.addCircle(circleOptions);

    }

    private final String KEY_GEOFENCE_LAT="GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON="GEOFENCE LONGITUDE";

    private void saveGeofence()
    {
        Log.d(TAG,"saveGeofence()");
        SharedPreferences sharedPreferences=getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putLong(KEY_GEOFENCE_LAT,Double.doubleToRawLongBits(geofenceMarker.getPosition().latitude));
        editor.putLong(KEY_GEOFENCE_LON,Double.doubleToRawLongBits(geofenceMarker.getPosition().longitude));
        editor.apply();
    }

    private void recoverGeofenceMarker()
    {
        Log.d(TAG,"recoverGeofenceMarker()");
        SharedPreferences sharedPreferences=getPreferences(Context.MODE_PRIVATE);
        if(sharedPreferences.contains(KEY_GEOFENCE_LAT)&&sharedPreferences.contains(KEY_GEOFENCE_LON))
        {
            double lat=Double.longBitsToDouble(sharedPreferences.getLong(KEY_GEOFENCE_LAT,-1));
            double lon=Double.longBitsToDouble(sharedPreferences.getLong(KEY_GEOFENCE_LON,-1));
            LatLng latLng=new LatLng(lat,lon);
            markerForGeofence(latLng);
            drawGeofence();

        }
    }

    private void clearGeofence()
    {
        Log.d(TAG,"clearGeofence()");

        LocationServices.GeofencingApi.removeGeofences(googleApiClient,createGeofencePendingIntent()).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess())
                {
                    removeGeofenceDraw();
                }
            }
        });
    }

    private void removeGeofenceDraw()
    {
        Log.d(TAG,"removeGeofenceDraw");
        if(geofenceMarker!=null)
            geofenceMarker.remove();
        if(geofenceLimits!=null)
            geofenceLimits.remove();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



}



