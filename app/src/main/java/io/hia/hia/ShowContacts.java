package io.hia.hia;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.nightonke.boommenu.BoomMenuButton;

import java.util.ArrayList;
import java.util.List;

public class ShowContacts extends AppCompatActivity implements AdapterView.OnItemClickListener {

    //Phone numbers list
        static List<String> NameList                = new ArrayList<>();
        static List<String> NumberList              = new ArrayList<>();
        static Boolean      NameAndNumberListReady  = false;

    //Divers
        private static   String MyPhoneNumber    = "";

    //Boom buttons !
        private boolean init = false;
        private BoomMenuButton boomMenuButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Load Layer
        //----------
            //Set title & icon
                setTitle(R.string.AppTitle);
            //Load Layout
                setContentView(R.layout.activity_show_contacts);
                getSupportActionBar().setLogo(R.drawable.icon_app);
                //boomMenuButton = (BoomMenuButton)findViewById(R.id.boom);

        //Firebase
        //--------
            UpdateToken();

        //Update Contact list
        //-------------------
            //Convert list to string
                String[]    NomContacts = new String[NameList.size()];
                NomContacts = NameList.toArray(NomContacts);

            //Creat adapter
                //ListAdapter testadapteur = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, NomContacts);
                ListAdapter testadapteur = new ContactRowAdapter(this, NomContacts);

            //Create list
                ListView contactsview = (ListView) findViewById(R.id.contactview_id);
                contactsview.setAdapter(testadapteur);
                contactsview.setOnItemClickListener(this);

            Log.d(MainActivity.LOG_TAG,"Show Contacts is displayed");

        //Start CheckHia service
        //----------------------
            if( isMyServiceRunning(HiaBackGroundService.class) == false) {
                Intent BackGroundServiceIntent = new Intent(this, HiaBackGroundService.class);
                startService(BackGroundServiceIntent);
            }
    }

    //////////////////////////////////////////////////
    //                                              //
    //                  onItemClick                 //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        ((BoomMenuButton)v.findViewById(R.id.boom_circle)).boom();
    }

    //////////////////////////////////////////////////
    //                                              //
    //                Boom Button                   //
    //                                              //
    //////////////////////////////////////////////////

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);


//        // Use a param to record whether the boom button has been initialized
//        // Because we don't need to init it again when onResume()
//        if (init) return;
//        init = true;
//
//        Drawable[] subButtonDrawables = new Drawable[3];
//        int[] drawablesResource = new int[]{
//                R.drawable.boom,
//                R.drawable.java,
//                R.drawable.github
//        };
//        for (int i = 0; i < 3; i++)
//            subButtonDrawables[i] = ContextCompat.getDrawable(this, drawablesResource[i]);
//
//        String[] subButtonTexts = new String[]{"BoomMenuButton", "View source code", "Follow me"};
//
//        int[][] subButtonColors = new int[3][2];
//        for (int i = 0; i < 3; i++) {
//            subButtonColors[i][1] = ContextCompat.getColor(this, R.color.material_white);
//            subButtonColors[i][0] = Util.getInstance().getPressedColor(subButtonColors[i][1]);
//        }
//
//     /*
//        // Now with Builder, you can init BMB more convenient
//        new BoomMenuButton.Builder()
//                .addSubButton(ContextCompat.getDrawable(this, R.drawable.boom), subButtonColors[0], "BoomMenuButton")
//                .addSubButton(ContextCompat.getDrawable(this, R.drawable.java), subButtonColors[0], "View source code")
//                .addSubButton(ContextCompat.getDrawable(this, R.drawable.github), subButtonColors[0], "Follow me")
//                .button(ButtonType.HAM)
//                .boom(BoomType.PARABOLA)
//                .place(PlaceType.HAM_3_1)
//                .subButtonTextColor(ContextCompat.getColor(this, R.color.black))
//                .subButtonsShadow(Util.getInstance().dp2px(2), Util.getInstance().dp2px(2))
//                .init(boomMenuButton);
//*/
//        boomMenuButton.init(
//                subButtonDrawables, // The drawables of images of sub buttons. Can not be null.
//                subButtonTexts,     // The texts of sub buttons, ok to be null.
//                subButtonColors,    // The colors of sub buttons, including pressed-state and normal-state.
//                ButtonType.HAM,     // The button type.
//                BoomType.PARABOLA,  // The boom type.
//                PlaceType.HAM_3_1,  // The place type.
//                null,               // Ease type to move the sub buttons when showing.
//                null,               // Ease type to scale the sub buttons when showing.
//                null,               // Ease type to rotate the sub buttons when showing.
//                null,               // Ease type to move the sub buttons when dismissing.
//                null,               // Ease type to scale the sub buttons when dismissing.
//                null,               // Ease type to rotate the sub buttons when dismissing.
//                null                // Rotation degree.
//        );
//
//        boomMenuButton.setTextViewColor(ContextCompat.getColor(this, R.color.black));
//        boomMenuButton.setSubButtonShadowOffset(Util.getInstance().dp2px(2), Util.getInstance().dp2px(2));
    }


    //////////////////////////////////////////////////
    //                                              //
    //                                              //
    //           SearchNameFromPhoneNumber          //
    //                                              //
    //                                              //
    //////////////////////////////////////////////////
    public static String SearchNameFromPhoneNumber(String phone){

        String Name = phone;

        for (int i = 0; i < NumberList.size(); i++) {
            if (NumberList.get(i).contentEquals(phone)) {
                Name = NameList.get(i);
                break;
            }
        }

        return Name;
    }

    //////////////////////////////////////////////////
    //                                              //
    //                                              //
    //            isMyServiceRunning                //
    //                                              //
    //                                              //
    //////////////////////////////////////////////////
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    //////////////////////////////////////////////////
    //                                              //
    //              SetMyPhoneNumber                //
    //                                              //
    //////////////////////////////////////////////////
    public static void SetMyPhoneNumber(String phone){
        MyPhoneNumber = phone;
    }
    //////////////////////////////////////////////////
    //                                              //
    //              GetMyPhoneNumber                //
    //                                              //
    //////////////////////////////////////////////////
    public static String GetMyPhoneNumber(){
        return MyPhoneNumber;
    }
    //////////////////////////////////////////////////
    //                                              //
    //                  UpdateToken                 //
    //                                              //
    //////////////////////////////////////////////////
    public void UpdateToken()
    {
        RegisterFirebaseToken registerFirebaseToken = new RegisterFirebaseToken(this);
        registerFirebaseToken.Update(MyPhoneNumber);
    }
}

