package io.hia.hia;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * Created by edouard on 10/10/16.
 */

public class GetContacts implements AsyncResponse {

    private Activity CurrentActivity;
    private ContentResolver ContactResolver;
    private final String AdrGetPhoneNumber = "CheckHiaContacts.php";

    private ArrayList<String> AllContactsNumbers = new ArrayList<String>();
    private ArrayList<String> AllContactsNames = new ArrayList<String>();
    private Boolean LoadShowContactsActivityWhenFinished;


    //////////////////////////////////////////////////
    //                                              //
    //                 Constructor                  //
    //                                              //
    //////////////////////////////////////////////////
    public GetContacts(Activity activity, ContentResolver contactresolver) {
        CurrentActivity = activity;
        ContactResolver = contactresolver;
    }

    //////////////////////////////////////////////////
    //                                              //
    //                    Get                       //
    //                                              //
    //////////////////////////////////////////////////
    public void Get(Boolean ...ShowContacts_Activity_WhenFinished)  {

        //Set Value of ShowContactsActivityWhenFinished
        //----------------------------------------------
            LoadShowContactsActivityWhenFinished = false;
            if(ShowContacts_Activity_WhenFinished.length == 1) {
                LoadShowContactsActivityWhenFinished = ShowContacts_Activity_WhenFinished[0];
            }

        //Phone list update is long, so it is execute in background
        //---------------------------------------------------------
            UpdatePhoneNumbersList updatePhoneNumbersList = new UpdatePhoneNumbersList();
            updatePhoneNumbersList.execute();
    }


    //////////////////////////////////////////////////
    //                                              //
    //                                              //
    //                  GetContact                  //
    //                                              //
    //                                              //
    //////////////////////////////////////////////////
    private class UpdatePhoneNumbersList extends AsyncTask<String, String, String> {

        //////////////////////////////////////////////////
        //                  doInBackground              //
        //////////////////////////////////////////////////
        @Override
        protected String doInBackground(String... strings) {

            return GetAllPhoneNumbers();
        }

        //////////////////////////////////////////////////
        //                  onPostExecute              //
        //////////////////////////////////////////////////
        @Override
        protected void onPostExecute(String PhoneList) {
            super.onPostExecute(PhoneList);

                try {
                    String Adr = AdrGetPhoneNumber + "?PhonesNumbers=" + URLEncoder.encode(PhoneList, "UTF-8"); // URLEncoder.encode(GetAllPhoneNumbers(), "UTF-8");

                    HttpTask nHttpTask = new HttpTask();
                    Log.d(MainActivity.LOG_TAG,"Load phone numbers list ! ");

                    nHttpTask.Set(CurrentActivity.getApplicationContext(), GetContacts.this, Adr);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

    //////////////////////////////////////////////////
    //                                              //
    //               processFinish                  //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public void processFinish(String output) {
        Log.d(MainActivity.LOG_TAG, "Data return : " + output);

        try {
            //JSON Conversion
            //---------------
            JSONObject JSON_Data = null;
            JSON_Data = new JSONObject(output);


            if(!JSON_Data.isNull("Resultat")) {

                ArrayList<String> PhoneNumberOkList = new ArrayList<String>();
                JSONArray jsonArray = new JSONArray(JSON_Data.getString("Resultat"));


                ShowContacts.NameAndNumberListReady = false;     //Set Flat to indication list is not update

                    //Delete previous contacts
                    ShowContacts.NumberList.clear();
                    ShowContacts.NameList.clear();

                    //Get phones numbers
                    //------------------
                        if (jsonArray != null) {
                            for (int i=0;i<jsonArray.length();i++){
                                ShowContacts.NumberList.add(jsonArray.get(i).toString());
                            }
                        }

                    //Get Names according to theses phones numbers
                    //--------------------------------------------
                        for(int i=0; i<ShowContacts.NumberList.size();i++)
                        {
                            ShowContacts.NameList.add(GetNameAccordingToPhoneNumers(ShowContacts.NumberList.get(i)));
                        }

                ShowContacts.NameAndNumberListReady = true;     //Set Flat to indication list is Update

                //Update contact list displayed
                //------------------------------
                    if(LoadShowContactsActivityWhenFinished) {
                        ShowContacts.SetMyPhoneNumber(MainActivity.GetMyPhoneNumber());
                        CurrentActivity.startActivity(new Intent(CurrentActivity.getApplicationContext(), ShowContacts.class));
                    }
            }else {
                //Something is incorrect
                Log.d(MainActivity.LOG_TAG, "Phone number feedback incorrect :" + output);
                Toast.makeText(CurrentActivity.getApplicationContext(), "Phone number feedback incorrect", Toast.LENGTH_SHORT).show();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    //////////////////////////////////////////////////
    //                                              //
    //               GetAllPhoneNumbers             //
    //                                              //
    //////////////////////////////////////////////////
    private String GetAllPhoneNumbers(){

        String Result = "[]";


            Cursor cursor = ContactResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            //If there is contacts
            if (cursor.moveToFirst()) {

                AllContactsNumbers.clear();
                AllContactsNames.clear();

                //Get All phones numbers
                //-----------------------
                do {
                    //Get Id of the contact
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String Name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String TmpNum;

                    //Check if there is phone numbers
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = ContactResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                        //Add all phones numbers
                        while (pCur.moveToNext()) {
                            String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));


                            //Convert to "standard" phone number
                            TmpNum = ConvertPhoneNumber(contactNumber);

                            //Add only Mobile phone number to the list
                            if(IsMobilePhoneNumber(TmpNum)) {
                                AllContactsNumbers.add(TmpNum);
                                AllContactsNames.add(Name);
                                break;
                            }
                        }
                        pCur.close();
                    }
                } while (cursor.moveToNext());

                //Convert Array to JSON
                //---------------------
                Result =  new JSONArray(AllContactsNumbers).toString();
            }


        //Log.d(MainActivity.LOG_TAG,"PhoneNumber ="+Result);
        return Result;
    }


    //////////////////////////////////////////////////
    //                                              //
    //          GetNameAccordingToPhoneNumers       //
    //                                              //
    //////////////////////////////////////////////////
    private String GetNameAccordingToPhoneNumers(String Number){

        //Found the name according to the phone number
            for(int i=0; i<AllContactsNumbers.size();i++)
            {
                if(Number.contentEquals(AllContactsNumbers.get(i))){

                    Log.d(MainActivity.LOG_TAG,  AllContactsNames.get(i) + ":" + Number );
                    return AllContactsNames.get(i);
                }
            }
        Log.d(MainActivity.LOG_TAG,  Number + "Inconnu :"+Number);
        return Number;
    }

    //////////////////////////////////////////////////
    //                                              //
    //               IsMobilePhoneNumber            //
    //                                              //
    //////////////////////////////////////////////////
    public static boolean IsMobilePhoneNumber(String PhoneNumber){

        if(PhoneNumber.length() == 13) {                            //If the lenght is OK
            if (PhoneNumber.substring(0, 5).equals("00336")) {      //And start with 00336
                return true;
            }

            if (PhoneNumber.substring(0, 5).equals("00337")) {      //Or start with 00337
                return true;
            }
        }
        return false;
    }


    //////////////////////////////////////////////////
    //                                              //
    //           SearchNameFromPhoneNumber          //
    //                                              //
    //////////////////////////////////////////////////
    public static String ConvertPhoneNumber(String PhoneNumber){

        //Delete spaces
            PhoneNumber = PhoneNumber.replace(" ","");

        //Delete "-"
            PhoneNumber = PhoneNumber.replace("-","");

        //Replace "+" => "00"
            PhoneNumber = PhoneNumber.replace("+","00");

        //Transform "06" => "00336"
            if(PhoneNumber.substring(0,2).equals("06")){
                PhoneNumber = "0033" + PhoneNumber.substring(1);
            }

        //Transform "07" => "00337"
            if(PhoneNumber.substring(0,2).equals("07")){
                PhoneNumber = "0033" + PhoneNumber.substring(1);
            }
        return PhoneNumber;
    }

    //////////////////////////////////////////////////
    //                                              //
    //               PhoneIsOffLine                 //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public void PhoneIsOffLine() {

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(CurrentActivity);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(CurrentActivity.getApplicationContext().getResources().getString(R.string.InternetConnexionError))
                .setTitle("Error");

        // 3. Add the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                CurrentActivity.finish();
                System.exit(0);
            }
        });

        // 4. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
