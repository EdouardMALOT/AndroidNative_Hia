package io.hia.hia;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by edouard on 05/10/16.
 */

//////////////////////////////////////////////////
//                                              //
//                                              //
//                SendToServer                  //
//                                              //
//                                              //
//////////////////////////////////////////////////
class SendToServer implements AsyncResponse, GPS_Response {

    private final String AdrSendHia = "SendHia.php";

    private String SendFrom;
    private String SendTo;
    private String SendToName;
    private String TypeHia;
    private double MyLatitude;
    private double MyLongitude;

    private Context CurrentContext;

    private HttpTask nHttpTask;

    public SendToServer(){
        nHttpTask = new HttpTask();
    }
    //////////////////////////////////////////////////
    //                                              //
    //                  SendHia                     //
    //                                              //
    //////////////////////////////////////////////////
    public void Send(Context context, String from, String to_name, String to_number, String typehia) {

         //Get params
            CurrentContext  = context;
            SendFrom        = from;
            SendToName      = to_name;
            SendTo          = to_number;
            TypeHia         = typehia;

        //Http
            if(!TypeHia.contentEquals("Hia"))
            {
                MyLatitude  = 0.0;
                MyLongitude = 0.0;
                HttpSend();     //Make the http request

            }else{
                Toast.makeText(CurrentContext, "Envoie en cours (Attente GPS)" , Toast.LENGTH_SHORT).show();

                //Wait until GPS position and then make Http request
               //new GPS_Position(CurrentContext, SendToServer.this);
               new GMS_Position(CurrentContext, SendToServer.this);

            }
    }

    //////////////////////////////////////////////////
    //                                              //
    //               GPS_position                   //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public void GPS_position(String Error, double latitude, double longitude) {

        //TODO : gérer
        MyLatitude  = latitude;
        MyLongitude = longitude;
        HttpSend();                     //Make the http request
    }

    //////////////////////////////////////////////////
    //                                              //
    //                   HttpSend                   //
    //                                              //
    //////////////////////////////////////////////////
    private void HttpSend(){

        //Send Hia with actual position
        //------------------------------
           Log.d(MainActivity.LOG_TAG,TypeHia + " send (" +SendFrom + "=>" + SendTo + ")");

            try {
                String Adr = AdrSendHia + "?From=" + URLEncoder.encode(SendFrom, "UTF-8") + "&To=" + URLEncoder.encode(SendTo, "UTF-8") + "&Type="+ TypeHia + "&Lat=" + MyLatitude + "&Lng=" + MyLongitude; //+"&I=" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
                nHttpTask.Set(CurrentContext, SendToServer.this, Adr);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    //////////////////////////////////////////////////
    //                                              //
    //               processFinish                  //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public void processFinish(String output) {

        //JSON Conversion
        //---------------
            JSONObject JSON_Response;
            try {
                JSON_Response = new JSONObject(output);
                //String Reponse = CurrentContext.getResources().getString(R.string.app_name) + " " + JSON_Response.getString("Titre") + " to " + SendToName;
                String Reponse = "WAU envoyé à " + SendToName;

                Toast.makeText(CurrentContext, Reponse , Toast.LENGTH_SHORT).show();
                Log.d(MainActivity.LOG_TAG, Reponse);

            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    //////////////////////////////////////////////////
    //                                              //
    //               PhoneIsOffLine                 //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public void PhoneIsOffLine() {
        Toast.makeText(CurrentContext, CurrentContext.getResources().getString(R.string.InternetConnexionError), Toast.LENGTH_SHORT).show();
    }

}