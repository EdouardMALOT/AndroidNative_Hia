package io.hia.hia;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class GPSActivity extends AppCompatActivity implements GPS_Response {

    private TextView LatitudeTxt;
    private TextView LongitudeTxt;

    //////////////////////////////////////////////////
    //                                              //
    //                  onCreate                    //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gps);

        LatitudeTxt     = (TextView) findViewById(R.id.Latlbl);
        LongitudeTxt    = (TextView) findViewById(R.id.Lnglbl);

        //Wait until a new GPS position
        //------------------------------
        //new GPS_Position(this, GPSActivity.this);
        new GMS_Position(this, GPSActivity.this);

    }

    //////////////////////////////////////////////////
    //                                              //
    //                  GPS_Position                //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public void GPS_position(String Error, double latitude, double longitude) {

        LatitudeTxt.setText("Lat ="+latitude);
        LongitudeTxt.setText("Lng ="+longitude);
    }
}
