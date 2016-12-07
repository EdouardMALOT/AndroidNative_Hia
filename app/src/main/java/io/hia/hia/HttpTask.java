package io.hia.hia;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by edouard on 10/10/16.
 */

public class HttpTask {

    //Definitions
        private final String Website          = "http://www.place2b.ovh/Hia/";
        private final int Nbhttpretry = 5;

        private Context CurrentContext;
        private Boolean ShowToast;

        private AsyncTask Task = null;
        public  AsyncResponse ReturnTask = null;

        private String StringUrl;

        Integer NbTimeOut;

    //////////////////////////////////////////////////
    //                                              //
    //                    Set                       //
    //                                              //
    //////////////////////////////////////////////////
    public void Set(Context context, AsyncResponse returntask, String action, Boolean ...showtoast) throws InterruptedException {

        //Apply default value to Show Toast
            ShowToast = true;
            if(showtoast.length == 1)
            {
                ShowToast = showtoast[0];  // Overrided Value
            }

        //Wait for previous Http request
            NbTimeOut = 0;
            while (Task != null && NbTimeOut++ < 100)  { Thread.sleep(50);  }       //Wait until previous Http request is finished or Timeout (100*50ms = 5sec)

        //If Task Wasn't finish cancel it.
            DestroyIfNeeded();

        //Check if phone is Online
        //------------------------
            if (!isOnline(context)){
                returntask.PhoneIsOffLine();
            }else {
                //Launch http task
                CurrentContext = context;
                ReturnTask = returntask;
                StringUrl = Website + action;

                Task = new HttpRequest().execute();
            }
    }

    //////////////////////////////////////////////////
    //                                              //
    //                DestroyIfNeeded               //
    //                                              //
    //////////////////////////////////////////////////
    public void DestroyIfNeeded() {

        //TODO : Appeler dans un destructeur !!!!
        if(Task!= null){
            Task.cancel(true);
             Log.d(MainActivity.LOG_TAG,  "HttpTask cancel !");
        }
    }

    //////////////////////////////////////////////////
    //                                              //
    //                                              //
    //                  GetContact                  //
    //                                              //
    //                                              //
    //////////////////////////////////////////////////
    private class HttpRequest extends AsyncTask<String, String, String> {

        //////////////////////////////////////////////////
        //                  doInBackground              //
        //////////////////////////////////////////////////
        @Override
        protected String doInBackground(String... strings) {

            int TryCount = 0;

            //Retry Nbhttpretry Time the connexion before leaving
            //-----------------------------------------
            while (TryCount < Nbhttpretry) {

                HttpURLConnection connection = null;
                BufferedReader reader = null;

                try {
                    //Connect and get datas
                    //---------------------

                        URL url = new URL(StringUrl);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.connect();

                        InputStream stream = connection.getInputStream();

                        reader = new BufferedReader(new InputStreamReader(stream));
                        StringBuffer buffer = new StringBuffer();
                        String line = "";

                        while ((line = reader.readLine()) != null) {
                            buffer.append(line);
                        }

                    //Return
                    //------
                        return buffer.toString();


                    //Exeptions
                    //---------
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (connection != null)
                            connection.disconnect();
                        try {
                            if (reader != null)
                                reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        TryCount++;
                    }
            }
            return CurrentContext.getResources().getString(R.string.ErrorConnexionImpossible);
        }

        //////////////////////////////////////////////////
        //                  onPostExecute              //
        //////////////////////////////////////////////////
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Boolean AnswerOk = false;
            String ErrorMsg = "";

            //Decode JSON
            //-----------
            JSONObject JSON_Data = null;
            try {
                JSON_Data = new JSONObject(result);

                //Check if there is no error from server
                if(JSON_Data.isNull("Error"))
                {
                    AnswerOk = true;
                }else{
                    ErrorMsg = JSON_Data.getString("Error");
                }

            } catch (JSONException e) {
                e.printStackTrace();
                ErrorMsg = "JSONException";
            }

            //Action
            //------
            Task = null;

            if(AnswerOk == true)
            {
                ReturnTask.processFinish(JSON_Data.toString());
            }else{
                ErrorMsg = "Error : " + ErrorMsg;
                Log.d(MainActivity.LOG_TAG,  ErrorMsg);
                if(ShowToast) {
                    Toast.makeText(CurrentContext, ErrorMsg, Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    //////////////////////////////////////////////////
    //                                              //
    //                                              //
    //                 isOnline                     //
    //                                              //
    // Check if there is an internet connexion      //
    //////////////////////////////////////////////////
    public boolean isOnline(Context context) {

        //Context context = getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }
}
