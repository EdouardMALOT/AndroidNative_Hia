package io.hia.hia;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by edouard on 28/10/16.
 */

public class RegisterFirebaseToken implements AsyncResponse{

    private final String UpdateFirebase = "UpdateFirebase.php";
    private Activity CurrentActivity;

    //////////////////////////////////////////////////
    //                                              //
    //                 Constructor                  //
    //                                              //
    //////////////////////////////////////////////////
    public RegisterFirebaseToken(Activity activity) {
        CurrentActivity = activity;
    }

    //////////////////////////////////////////////////
    //                                              //
    //                    Update                    //
    //                                              //
    //////////////////////////////////////////////////
    public void Update(String MyPhoneNumber) {

        try {
            Log.d(MainActivity.LOG_TAG,"Send Firebase Token to server");

            String Adr = UpdateFirebase + "?Phone=" + URLEncoder.encode(MyPhoneNumber,"UTF-8") + "&Token=" + FirebaseInstanceId.getInstance().getToken(); // URLEncoder.encode(GetAllPhoneNumbers(), "UTF-8");

            HttpTask nHttpTask = new HttpTask();
            nHttpTask.Set(CurrentActivity.getApplicationContext(), RegisterFirebaseToken.this, Adr);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void processFinish(String output) {

        try {
            JSONObject JSON_Data = null;
            JSON_Data = new JSONObject(output);

            if ( !JSON_Data.isNull("Result") ){
                Log.d(MainActivity.LOG_TAG,JSON_Data.getString("Result"));
            }else{
                Log.d(MainActivity.LOG_TAG, "RegisterFirebaseToken Data return : " + output);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void PhoneIsOffLine() {

    }

}
