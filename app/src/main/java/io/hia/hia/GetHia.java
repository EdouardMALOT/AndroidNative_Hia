package io.hia.hia;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;

import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by edouard on 05/10/16.
 */

class GetHia implements AsyncResponse {

    private final String AdrGetHia = "GetHia.php";
    private final String AdrAckHia = "AckHia.php";

    private HttpTask nHttpTask = null;

    private Context  CurrentContext          = null;

    protected Boolean NewHia                 = false;
    protected String ReceivedHiaId           = null;
    protected String ReceivedFromName        = null;
    protected String ReceivedFromNumber      = null;
    protected String Me                      = null;

    protected String ReceivedHiaType         = null;
    protected double ReceivedHiaLatitude     = 0.0;
    protected double ReceivedHiaLongitude    = 0.0;
    protected String ReceivedHiaState        = null;

    private boolean GetHiaInProcess          =   false;
    private boolean AckInProcess             =   false;

    private static final String KEY_TEXT_WAU_QUESTION = "key_wau_question_reply";


    //////////////////////////////////////////////////
    //                                              //
    //                 Constructor                  //
    //                                              //
    //////////////////////////////////////////////////
    public GetHia(){
        nHttpTask = new HttpTask();
    }

    //////////////////////////////////////////////////
    //                                              //
    //                      Get                     //
    //                                              //
    //////////////////////////////////////////////////
    public void Get(Context context, String me) {

    //Launch http task
    //----------------
        //If previous transaction not ended
        //---------------------------------
            if(GetHiaInProcess == true)
            {
                nHttpTask.DestroyIfNeeded();
                GetHiaInProcess = false;
            }

            if(AckInProcess == true)
            {
                nHttpTask.DestroyIfNeeded();
                AckInProcess = false;
            }

            CurrentContext = context;
            Me = me;

            try {
                String Adr = AdrGetHia + "?For=" + URLEncoder.encode(Me, "UTF-8");
                GetHiaInProcess = true;
                nHttpTask.Set(CurrentContext,this,Adr,false);                           //Do not show toast !

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

        try {
            //JSON Conversion
            //---------------
            JSONObject JSON_Data = null;
            JSON_Data = new JSONObject(output);


            if (AckInProcess == false) {

                //Check if there is no error from server
                if (!JSON_Data.isNull("Nothing")) {
                    Log.d(MainActivity.LOG_TAG, "Nothing for me");

                } else if ((!JSON_Data.isNull("Id")) && (!JSON_Data.isNull("HiaFrom")) && (!JSON_Data.isNull("Type")) && (!JSON_Data.isNull("Latitude")) && (!JSON_Data.isNull("Longitude")) && (!JSON_Data.isNull("State"))) {

                    //Read params
                    ReceivedHiaId = JSON_Data.getString("Id");

                    ReceivedFromNumber = JSON_Data.getString("HiaFrom");
                    ReceivedFromName = ShowContacts.SearchNameFromPhoneNumber(ReceivedFromNumber);

                    ReceivedHiaType = JSON_Data.getString("Type");

                    ReceivedHiaLatitude = JSON_Data.getDouble("Latitude");
                    ReceivedHiaLongitude = JSON_Data.getDouble("Longitude");
                    ReceivedHiaState = JSON_Data.getString("State");

                    NewHia = true;

                    Log.d(MainActivity.LOG_TAG, ReceivedHiaType+ " received from :" + ReceivedFromName);


                    //Ack Hia received !
                    //------------------
                        String Adr = AdrAckHia + "?Id=" + URLEncoder.encode(ReceivedHiaId, "UTF-8");
                        AckInProcess = true;
                        nHttpTask.Set(CurrentContext, this, Adr);

                } else {
                    //Some params missed
                    Log.d(MainActivity.LOG_TAG, "JSON_Data received not complete : " + output);
                }

                //Release GetHia in progress flag
                //-------------------------------
                    GetHiaInProcess = false;

            } else {
                if (JSON_Data.getString("Result").contentEquals("OK")) {

                    Log.d(MainActivity.LOG_TAG, "Ack done");

                    CreateNotification();

                    //Check if there is other Hia
                    //---------------------------
                        String Adr = AdrGetHia + "?For=" + URLEncoder.encode(Me, "UTF-8");
                        GetHiaInProcess = true;
                        nHttpTask.Set(CurrentContext,this,Adr,false);                           //Do not show toast !

                }else{
                    //Something is incorrect
                    Log.d(MainActivity.LOG_TAG, "ACK suspect " + output);
                }

                //Release Ack in progress flag
                //-------------------------------
                    AckInProcess = false;
            }

        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

    }

    //////////////////////////////////////////////////
    //                                              //
    //               PhoneIsOffLine                 //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public void PhoneIsOffLine() {
        //Toast.makeText(CurrentContext, CurrentContext.getResources().getString(R.string.InternetConnexionError), Toast.LENGTH_SHORT).show();
        Log.d(MainActivity.LOG_TAG, CurrentContext.getResources().getString(R.string.InternetConnexionError));
    }

    //////////////////////////////////////////////////
    //                                              //
    //              CreateNotification              //
    //                                              //
    //////////////////////////////////////////////////
    private void CreateNotification() {

        int NotificationUniqueId = HiaBackGroundService.getUniqueId();

        //Create Notification
            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(CurrentContext);

        //If Hia
        //------
            if (ReceivedHiaType.contentEquals("HiaQuestion")) {
                //What happen when you will click on button
                //-----------------------------------------

                   //Accept intent
                        Intent QuestionAcceptIntent = new Intent(CurrentContext, HiaQuestionAnswer.class);

                        //Put datas
                        Bundle Hiabundle = new Bundle();
                            Hiabundle.putString("HiaFromName", ReceivedFromName);
                            Hiabundle.putString("HiaFromNumber", ReceivedFromNumber);
                            Hiabundle.putInt("NotificationId", NotificationUniqueId);

                            QuestionAcceptIntent.putExtras(Hiabundle);

                        QuestionAcceptIntent.setAction("Accepted");

                        TaskStackBuilder HiaQuestionTaskAccepted = TaskStackBuilder.create(CurrentContext);
                        HiaQuestionTaskAccepted.addParentStack(HiaQuestionAnswer.class);
                        HiaQuestionTaskAccepted.addNextIntent(QuestionAcceptIntent);

                        PendingIntent pIntentAccept = HiaQuestionTaskAccepted.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                        //PendingIntent pIntentAccept = PendingIntent.getActivity(CurrentContext, 0, QuestionAcceptIntent, 0);

                    //Refused intent
                        Intent RefusedIntent = new Intent(CurrentContext, HiaQuestionAnswer.class);
                        RefusedIntent.putExtras(Hiabundle);         //Put datas
                        RefusedIntent.setAction("Refused");         //Action

                        TaskStackBuilder HiaQuestionTaskRefused= TaskStackBuilder.create(CurrentContext);
                        HiaQuestionTaskRefused.addParentStack(HiaQuestionAnswer.class);
                        HiaQuestionTaskRefused.addNextIntent(RefusedIntent);

                        PendingIntent pIntentRefused = HiaQuestionTaskRefused.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                        //PendingIntent pIntentRefused = PendingIntent.getActivity(CurrentContext, 0, RefusedIntent, 0);


                //Notification
                        nBuilder.setSmallIcon(R.drawable.position)
                            .setContentTitle(CurrentContext.getResources().getString(R.string.app_name))
                            .setContentText( ReceivedFromName+" souhaite connaitre votre position")
                            .addAction(R.drawable.quantum_ic_keyboard_arrow_down_white_36, "Accepter", pIntentAccept)
                            .addAction(R.drawable.ic_close_dark, "Refuser", pIntentRefused);

                //Notication builder params
                        nBuilder.setDefaults(Notification.DEFAULT_SOUND)
                            .setVibrate(new long[]{100, 200, 100, 200, 100, 200, 100, 200})
                            .setLights(Color.BLUE, 400, 400);

                        nBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

            }else if (ReceivedHiaType.contentEquals("Hia")) {

                //Intent en clicked
                    Intent MapIntent = new Intent();

                    MapIntent.setClass(CurrentContext, MapsActivity.class);

                //Put datas
                    Bundle Hiabundle = new Bundle();
                        Hiabundle.putString("HiaFrom", GetHiaFrom());
                        Hiabundle.putDouble("Latitude", GetHiaLatitude());
                        Hiabundle.putDouble("Longitude", GetHiaLongitude());

                        MapIntent.putExtras(Hiabundle);

                PendingIntent pIntent = PendingIntent.getActivity(CurrentContext, 0, MapIntent, 0);

                nBuilder.setContentIntent(pIntent)
                        .setContentTitle(CurrentContext.getResources().getString(R.string.app_name))
                        .setContentText(ReceivedFromName + " se trouve ici !")
                        .setSmallIcon(R.drawable.position)
                        .setAutoCancel(true);

                nBuilder.setDefaults(Notification.DEFAULT_SOUND)
                        .setVibrate(new long[]{100, 200, 100, 200})
                        .setLights(Color.BLUE, 400, 400);
            }else {
                nBuilder.setContentTitle("Une pensée !")
                        .setContentText(ReceivedFromName + " pense à vous !")
                        .setSmallIcon(R.drawable.position);

                nBuilder.setDefaults(Notification.DEFAULT_SOUND)
                        .setVibrate(new long[]{100, 200, 100, 200, 100, 200})
                        .setLights(Color.CYAN, 400, 400);
            }

        //Build notification
        //------------------

            //Manager
            NotificationManager notificationManager = (NotificationManager) CurrentContext.getSystemService(CurrentContext.NOTIFICATION_SERVICE);

            //Notification
                Notification notification = nBuilder.build();

            notificationManager.notify(NotificationUniqueId, notification);
    }

    //////////////////////////////////////////////////
    //                                              //
    //                  GetHiaFrom                  //
    //                                              //
    //////////////////////////////////////////////////
    public String GetHiaFrom(){

        return ReceivedFromName;
        /*
        if(GetFromServer.NewHia  == true)
        {
            return GetFromServer.ReceivedHiaHiaFrom;
        }else{
            return "?";
        }
        */
    }
    //////////////////////////////////////////////////
    //                                              //
    //               GetHiaLatitude                 //
    //                                              //
    //////////////////////////////////////////////////
    public double GetHiaLatitude(){

        return (ReceivedHiaLatitude);
    }
    //////////////////////////////////////////////////
    //                                              //
    //               GetHiaLongitude                 //
    //                                              //
    //////////////////////////////////////////////////
    public double GetHiaLongitude(){

        return (ReceivedHiaLongitude);
    }
}