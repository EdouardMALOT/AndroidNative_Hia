package io.hia.hia;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.content.pm.Signature;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends AppCompatActivity  {

    //Divers
        //Definitions
        public static       String LOG_TAG          = "HiaApp";
        public static final String AppSharedName    = "HiaPrefs";

    //Phone numbers list
        private static      String MyPhoneNumber    = "";

    //Permissions
        private static final int MY_PERMISSIONS_REQUEST    = 123;


    //////////////////////////////////////////////////
    //                                              //
    //                  onCreate                    //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //startActivity(new Intent(MainActivity.this, GPSActivity.class));

        //ShowHashKey();                //Only for the first time

        //Check if We have acces to Contacts
        //----------------------------------
        if (VerifyPermissions() == true) {
            //Permission are already OK
                ContinueAfterPermission();
        } else {
            //Permission are displayed to user
        }
    }
    //////////////////////////////////////////////////
    //                                              //
    //              VerifyPermissions               //
    //                                              //
    //////////////////////////////////////////////////
    void ContinueAfterPermission(){

        //Load Layout
        //-----------
        setContentView(R.layout.activity_main);

        //Check if there is stores values
        //-------------------------------
        SharedPreferences prefs = getSharedPreferences(AppSharedName, MODE_PRIVATE);
        //Delete value
        //prefs.edit().remove("PhoneNumber").commit();

        if (prefs.contains("PhoneNumber")) {
            MyPhoneNumber = prefs.getString("PhoneNumber", "");
            Log.v(LOG_TAG, "MyPhoneNumber=" + MyPhoneNumber);

            //Show contacts list
            //------------------
            GetContacts nGetContacts = new GetContacts(this, getContentResolver());
            nGetContacts.Get(true);                          //When finisher, it will call ShowContactsActivity
        } else {
            startActivity(new Intent(MainActivity.this, NewUser.class));
        }
    }


    //////////////////////////////////////////////////
    //                                              //
    //              VerifyPermissions               //
    //                                              //
    //////////////////////////////////////////////////
    public boolean VerifyPermissions() {

            if (        (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)        != PackageManager.PERMISSION_GRANTED)
                    ||  (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)   ) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{
                                                Manifest.permission.READ_CONTACTS,
                                                Manifest.permission.ACCESS_FINE_LOCATION},
                                              MY_PERMISSIONS_REQUEST);
                    }
                return false;
            }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if ( (grantResults.length >= 2) && (grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)  ) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    ContinueAfterPermission();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                    alertDialogBuilder.setMessage("Ces autorisations sont indispensable au fonctionnement de l'appli. \nVeuillez les activer puis relancer l'appli")
                                      .setPositiveButton("OK",
                                                            new DialogInterface.OnClickListener(){
                                                                public void onClick(DialogInterface dialog, int id){
                                                                    finish();
                                                                    System.exit(0);
                                                                }
                                                            });
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    //////////////////////////////////////////////////
    //                                              //
    //                                              //
    //              GetMyPhoneNumber                //
    //                                              //
    //                                              //
    //////////////////////////////////////////////////
    public static String GetMyPhoneNumber(){
        return MyPhoneNumber;
    }

    //////////////////////////////////
    //                              //
    //                              //
    //          ShowHashKey         //
    //                              //
    //                              //
    //////////////////////////////////
    private void ShowHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("io.hia.hia", PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d(LOG_TAG + "/KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}

