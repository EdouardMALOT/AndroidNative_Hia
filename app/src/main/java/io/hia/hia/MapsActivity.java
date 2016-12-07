package io.hia.hia;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //VARIABLES
    private GoogleMap mMap;

    private Timer TimerMoveToMyFriendLocation = null;                   //Timer for initial move to my location delay !
    private Timer TimerUpdateZoomAfterIntro = null;

    private final int MoveToMyFriendLocationDelay = 1000;        //Delais de 1.5secondes avant de commencer le zoom

    private final double DefaultZoom = 13.0;
    private final int ZoomAnimationDuration = 1500;              //Durée du zoom


    String HiaFrom = "Inconnu";
    Double HiaLatitude = 0.0;
    Double HiaLongitude = 0.0;

    //////////////////////////////////////////////////
    //                                              //
    //                                              //
    //                  onCreate                    //
    //                                              //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Reset Notification
        //=> We have set      setAutoCancel(true);
                    //NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    //nm.cancel(1);

        //Read extras data
            //Hia From
            if (getIntent().hasExtra("HiaFrom")) {
                HiaFrom = getIntent().getStringExtra("HiaFrom");
            }

             //Latitude
            if (getIntent().hasExtra("Latitude")) {
                HiaLatitude = getIntent().getDoubleExtra("Latitude", HiaLatitude);
            }

            //Longitude
            if (getIntent().hasExtra("Longitude")) {
                HiaLongitude = getIntent().getDoubleExtra("Longitude", HiaLongitude);
            }


        //Show Layout
            setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        //Return button
            ImageView Bp_return =(ImageView)findViewById(R.id.bp_activity_map_return_ShowContacts);

            Bp_return.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MapsActivity.this, ShowContacts.class));                   //Go to ShowContacts
                }
            });

    }


    //////////////////////////////////////////////////
    //                                              //
    //                                              //
    //                  onDestroy                   //
    //                                              //
    //                                              //
    // Call before close App, destroy all AsyncTask //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (TimerMoveToMyFriendLocation != null) {
            TimerMoveToMyFriendLocation.cancel();
            TimerMoveToMyFriendLocation.purge();
        }

        if (TimerUpdateZoomAfterIntro != null) {
            TimerUpdateZoomAfterIntro.cancel();
            TimerUpdateZoomAfterIntro.purge();
        }
    }


    //////////////////////////////////////////////////
    //                                              //
    //                                              //
    //                  onMapReady                  //
    //                                              //
    //                                              //
    //////////////////////////////////////////////////

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Active GPS and Zoom controls
        //----------------------------
            if (mMap != null) {
                if (!(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                    mMap.setMyLocationEnabled(true);
                }

                mMap.getUiSettings().setZoomControlsEnabled(true);                              //Ajout du controle de Zoom
            }

        // Add a maker on my friend position
        //----------------------------------
            //Position
                LatLng MyFriendLocation = new LatLng(HiaLatitude, HiaLongitude);
            //Title
                mMap.addMarker(new MarkerOptions().position(MyFriendLocation).title(HiaFrom)).showInfoWindow();;

        //Show Map
        //--------
             mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(0.0, 0.0)));

        //Moves  camera
        //-------------
            TimerMoveToMyFriendLocation = new Timer();
            TimerMoveToMyFriendLocation.schedule(new MoveToMyFriendLocation(), MoveToMyFriendLocationDelay);
    }


    //////////////////////////////////////////////////
    //                                              //
    //                                              //
    //             MoveToMyLocation                 //
    //                                              //
    //                                              //
    //////////////////////////////////////////////////
    class MoveToMyFriendLocation extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    //Moves the camera to users current longitude and latitude
                    //--------------------------------------------------------
                    LatLng latlng = new LatLng(HiaLatitude, HiaLongitude);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, (float) DefaultZoom), ZoomAnimationDuration, null); //Animates camera and zooms to preferred state on the user's current location.;

                    //UpdateZoomAfterIntro
                    //--------------------
                    TimerUpdateZoomAfterIntro = new Timer();
                    TimerUpdateZoomAfterIntro.schedule(new UpdateZoomAfterIntro(), ZoomAnimationDuration);

                }
            });
        }
    }

    //////////////////////////////////////////////////
    //                                              //
    //                                              //
    //             UpdateZoomAfterIntro             //
    //                                              //
    //                                              //
    //////////////////////////////////////////////////
    class UpdateZoomAfterIntro extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UpdateZoom();
                }
            });
        }
    }

    //////////////////////////////////////////////////
    //                                              //
    //                                              //
    //                  UpdateMode                  //
    //                                              //
    //                                              //
    //                                              //
    //////////////////////////////////////////////////
    private void UpdateZoom() {

        //Update Zoom according to Bounds
        //-------------------------------
            //Compute Bounds
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            //Make Bounds according to :
                //My location
                    //Location location = GPS_Position.GetLastKnowLocation(this);

                    //if(location != null){
                    //    builder.include(new LatLng(location.getLatitude(), location.getLongitude()));
                    //}

                //My friend location
                    builder.include(new LatLng(HiaLatitude, HiaLongitude));

                //Add false point in order to have a minimum zomm
                    double RoundLeftLongitude =  HiaLongitude-0.001;
                    double RoundRightLongitude = HiaLongitude+0.001;

                    builder.include(new LatLng(HiaLatitude, RoundLeftLongitude));
                    builder.include(new LatLng(HiaLatitude, RoundRightLongitude));

                LatLngBounds bounds = builder.build();

            //Update camera
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150)); // 200 =offset from edges of the map in pixels
    }

}
