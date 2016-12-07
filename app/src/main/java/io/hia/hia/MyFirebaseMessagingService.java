package io.hia.hia;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {

           Log.d(MainActivity.LOG_TAG, "Firebase msg received !!!");

           //Get Phone number
           //----------------
                String PhoneNum = null;

                SharedPreferences prefs = getSharedPreferences(MainActivity.AppSharedName, MODE_PRIVATE);
                if(prefs.contains("PhoneNumber")) {
                    PhoneNum = prefs.getString("PhoneNumber", "");
                }

           //Check for Hia !
           //---------------
               Log.d(MainActivity.LOG_TAG, "Check for a new "+ getResources().getString(R.string.app_name)+" ! ("+ PhoneNum +")");
               GetHia nGetHia = new GetHia();
               nGetHia.Get(getApplicationContext(), PhoneNum);
       }
}
