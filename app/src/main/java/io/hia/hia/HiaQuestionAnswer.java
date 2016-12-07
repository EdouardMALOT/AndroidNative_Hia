package io.hia.hia;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by edouard on 14/10/16.
 */


public class HiaQuestionAnswer extends Activity {

    String HiaFromName;
    String HiaFromNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        //Action
        //------
        if (intent.getAction() == "Accepted") {

            //Send Position
            //------------
            if (intent.hasExtra("HiaFromName") && intent.hasExtra("HiaFromNumber")) {
                HiaFromName = intent.getStringExtra("HiaFromName");
                HiaFromNumber = intent.getStringExtra("HiaFromNumber");

                SendToServer nSendToServer = new SendToServer();
                nSendToServer.Send(this.getApplicationContext(), ShowContacts.GetMyPhoneNumber(), HiaFromName, HiaFromNumber, "Hia");
            }

            Log.d(MainActivity.LOG_TAG, "Hia question from " + HiaFromName + " accepted !");
        } else {
            Log.d(MainActivity.LOG_TAG, "Hia question from " + HiaFromName + " refused !");
        }


        //Reset Notification
        //-------------------
            //GET the notification Id
                int Id = intent.getIntExtra("NotificationId",0);

            //Cancel notification
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.cancel(Id);

            Log.d(MainActivity.LOG_TAG, "Notification cancel, Id =" + Id);

        //Close Activity
        this.finish();
    }
}

