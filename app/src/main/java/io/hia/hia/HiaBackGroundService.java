package io.hia.hia;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class HiaBackGroundService extends Service {

    //Check Hia
        Handler CheckHiahandler = new Handler();
        private final int TimerCheckHia_Delay = 10*60000;          //Check every 10min

        GetHia nGetHia = null;
        String PhoneNum = null;

        private static int NotificationUniqueId = 10;

    //////////////////////////////////////////////////
    //                                              //
    //               onStartCommand                 //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Get phone number
        //----------------
            SharedPreferences prefs = getSharedPreferences(MainActivity.AppSharedName, MODE_PRIVATE);
            if(prefs.contains("PhoneNumber"))
            {
                PhoneNum = prefs.getString("PhoneNumber", "");
            }


        //Launch periodic check of received Hia
        //-------------------------------------
            nGetHia = new GetHia();
            CheckHiahandler.post(CheckHia);
        return START_STICKY;
    }

    //////////////////////////////////////////////////
    //                                              //
    //                   CheckHia                   //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public void onDestroy() {

        //Restart BackGroundService when he is destroyed
            Intent intent = new Intent();
            intent.setAction("RestartHiaBackGroundService");
            sendBroadcast(intent);

        super.onDestroy();
    }

    //////////////////////////////////////////////////
    //                                              //
    //                   CheckHia                   //
    //                                              //
    //////////////////////////////////////////////////
    private Runnable CheckHia = new Runnable() {

        @Override
        public void run() {

        if(PhoneNum == null)
        {
            //Update phone number if needed
            //-----------------------------
                SharedPreferences prefs = getSharedPreferences(MainActivity.AppSharedName, MODE_PRIVATE);
                if(prefs.contains("PhoneNumber"))
                {
                    PhoneNum = prefs.getString("PhoneNumber", "");
                }
        }else{
            //Check for Hia !
            //---------------
                Log.d(MainActivity.LOG_TAG, "Check for a new "+ getResources().getString(R.string.app_name)+" ! ("+ PhoneNum +")");
                nGetHia.Get(getApplicationContext(), PhoneNum);
        }

        //PostDelay for next execution
        CheckHiahandler.postDelayed(CheckHia, TimerCheckHia_Delay);
        }
    };


    //////////////////////////////////////////////////
    //                                              //
    //                   onBind                     //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //////////////////////////////////////////////////
    //                                              //
    //                   getUniqueId                //
    //                                              //
    //////////////////////////////////////////////////
    public static int getUniqueId(){
        int value = NotificationUniqueId++;
        return value;
    }
}
