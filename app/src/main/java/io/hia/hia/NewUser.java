package io.hia.hia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NewUser extends AppCompatActivity implements AsyncResponse {

    //VARIABLES
    //---------
        private final String AdrRegisterPhoneNumber = "RegisterPhoneNumber.php";
        private String PhoneNumber = "";

    //////////////////////////////////////////////////
    //                                              //
    //                  onCreate                    //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newuser);

        Button button = (Button) findViewById(R.id.button_envoyer_phone_number);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Launch http task
                    TextView Phone = (TextView) findViewById(R.id.editTextphonenumber);

                //Convert Phone number
                    PhoneNumber = GetContacts.ConvertPhoneNumber(Phone.getText().toString());

                //Send Phone Number if it is correct
                    if(GetContacts.IsMobilePhoneNumber(PhoneNumber)) {

                        try {
                            String Adr = AdrRegisterPhoneNumber + "?Phone=" + URLEncoder.encode(PhoneNumber, "UTF-8");
                            HttpTask nHttpTask = new HttpTask();
                            nHttpTask.Set(getApplicationContext(), NewUser.this, Adr);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }else{
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.ErrorIsNotaPhoneNumber), Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }

    //////////////////////////////////////////////////
    //                                              //
    //               processFinish                  //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public void processFinish(String output) {

    //Check feedback
    //--------------
        if (output.contentEquals("{\"Result\":\"OK\"}")) {

            //Save Datas
            SharedPreferences settings = getSharedPreferences(MainActivity.AppSharedName, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("PhoneNumber", PhoneNumber);
            // Commit the edits!
            editor.commit();

            Log.d(MainActivity.LOG_TAG, "New phone number added");

            //Back to main screen
            startActivity(new Intent(NewUser.this, MainActivity.class));

        } else {
            String ErroMsg = "Error in Register phone number process : " + output;
            Log.d(MainActivity.LOG_TAG, ErroMsg);

            Toast.makeText(getApplicationContext(), ErroMsg, Toast.LENGTH_SHORT).show();
        }
    }

    //////////////////////////////////////////////////
    //                                              //
    //               PhoneIsOffLine                 //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public void PhoneIsOffLine() {
        Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.InternetConnexionError), Toast.LENGTH_SHORT).show();
    }
}

