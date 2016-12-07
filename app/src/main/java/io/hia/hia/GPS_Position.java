//package io.hia.hia;
//
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.location.Criteria;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.provider.Settings;
//import android.support.v7.app.AlertDialog;
//import android.util.Log;
//
///**
// * Created by edouard on 15/10/16.
// */
//
////////////////////////////////////////////////////
////                                              //
////               Constructor                    //
////                                              //
////////////////////////////////////////////////////
//public class GPS_Position {
//
//    private LocationManager locationManager;
//    private LocationListener locationListener;
//    private Context CurrentContext;
//    private GPS_Response ReturnTask;
//
//    private long TimeOutDelay = 20000L;      //Time out de 20 secondes par défaut
//
//    //////////////////////////////////////////////////
//    //                                              //
//    //               Constructor                    //
//    //                                              //
//    //////////////////////////////////////////////////
//    public GPS_Position(Context context, GPS_Response returntask, long... time_out_delay) {
//
//        CurrentContext = context;
//        ReturnTask = returntask;
//        locationManager = (LocationManager) CurrentContext.getSystemService(CurrentContext.LOCATION_SERVICE);
//
//        //Update timeout delay if needed
//        if (time_out_delay.length > 1) {
//            TimeOutDelay = time_out_delay[0];
//        }
//
//
//        //Check if GPS is activate
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//
//            Log.d(MainActivity.LOG_TAG, "GPS : ON");
//            //If Yes launch a request
//            ConfigGPS();
//        } else {
//            //Else Show GPS setting to the user
//            showGPSDisabledAlertToUser();
//        }
//    }
//
//    //////////////////////////////////////////////////
//    //                                              //
//    //                  ConfigGPS                   //
//    //                                              //
//    //////////////////////////////////////////////////
//    private void ConfigGPS() {
//        locationListener = new MyLocationListener();
//
//        if (locationListener != null) {
//            ((MyLocationListener) locationListener).onStart();
//            locationManager.requestLocationUpdates("gps", 1, 1, locationListener);
//        }
//    }
//
//    //////////////////////////////////////////////////
//    //                                              //
//    //                  ConfigGPS                   //
//    //                                              //
//    //////////////////////////////////////////////////
//    public void Stop() {
//
//        //Remove listener
//        if (locationListener != null && locationManager != null) {
//            locationManager.removeUpdates(locationListener);
//        }
//
//        //Log
//        Log.d(MainActivity.LOG_TAG, "GPS : OFF");
//    }
//
//    //////////////////////////////////////////////////
//    //                                              //
//    //           showGPSDisabledAlertToUser         //
//    //                                              //
//    //////////////////////////////////////////////////
//    public class MyLocationListener implements LocationListener {
//        Handler time_out_task;
//
//        public void onStart() {
//            time_out_task = new Handler();
//            time_out_task.postDelayed(new Time_out_GPS(), TimeOutDelay);   //10 secondes TimeOUT
//        }
//
//        @Override
//        public void onLocationChanged(Location location) {
//
//            //Delete TimeOut Task. GPS arrived before
//            ((MyLocationListener) locationListener).time_out_task.removeCallbacksAndMessages(null);
//
//            //Give GPS Position
//            Log.d(MainActivity.LOG_TAG, "GPS position update !");
//            if (location != null) {
//                ReturnTask.GPS_position("OK", location.getLatitude(), location.getLongitude());
//            } else {
//                ReturnTask.GPS_position("Error", 0.0, 0.0);
//            }
//            Stop();
//        }
//
//        @Override
//        public void onStatusChanged(String s, int i, Bundle bundle) {
//
//        }
//
//        @Override
//        public void onProviderEnabled(String s) {
//
//        }
//
//        @Override
//        public void onProviderDisabled(String s) {
//            Log.d(MainActivity.LOG_TAG, "GPS : Disable => Prompt GPS settings to the user");
//
//            //Go to GPS Setting if GPS is OFF
//            //-------------------------------
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            CurrentContext.startActivity(intent);
//        }
//
//    }
//
//    //////////////////////////////////////////////////
//    //                                              //
//    //                  Time_out_GPS                //
//    //                                              //
//    //////////////////////////////////////////////////
//    class Time_out_GPS implements Runnable {
//        public void run() {
//            Log.d(MainActivity.LOG_TAG, "GPS TimeOUT !");
//
//            Location location = GetLastKnowLocation(CurrentContext);
//
//            if (location != null) {
//                ReturnTask.GPS_position("TimeOut", location.getLatitude(), location.getLongitude());
//            } else {
//                ReturnTask.GPS_position("TimeOut + Error", 0.0, 0.0);
//            }
//            Stop();
//        }
//    }
//
//
//    //////////////////////////////////////////////////
//    //                                              //
//    //           showGPSDisabledAlertToUser         //
//    //                                              //
//    //////////////////////////////////////////////////
//    private void showGPSDisabledAlertToUser() {
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CurrentContext);
//        alertDialogBuilder.setMessage("La localisation doit être activée.")
//                .setPositiveButton("Menu localisation",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                                CurrentContext.startActivity(callGPSSettingIntent);
//                            }
//                        });
//        AlertDialog alert = alertDialogBuilder.create();
//        alert.show();
//    }
//
//    //////////////////////////////////////////////////
//    //                                              //
//    //              GetLastKnowLocation             //
//    //                                              //
//    //////////////////////////////////////////////////
//    public static Location GetLastKnowLocation(Context context) {
//
//        Location location = null;
//        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
//
//        if (locationManager != null) {
//            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//            //If there is no last position according to GPS
//            //---------------------------------------------
//            if (location == null) {
//
//                //Get a lower accurate GPS Position
//                //---------------------------------
//                    Criteria criteria = new Criteria();
//                    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
//                    String provider = locationManager.getBestProvider(criteria, true);
//                    location = locationManager.getLastKnownLocation(provider);
//
//                    Log.d(MainActivity.LOG_TAG, "Get last GPS : fine accuracy");
//            }else{
//                    Log.d(MainActivity.LOG_TAG, "Get last GPS : Coarse accuracy");
//            }
//        }else{
//                    Log.d(MainActivity.LOG_TAG, "Get last GPS : cannot getSystemService(context.LOCATION_SERVICE)");
//        }
//
//        Log.d(MainActivity.LOG_TAG, "Last GPS : lat:" + location.getLatitude() + " , Lng:"+location.getLongitude());
//
//        return location;
//    }
//
//}