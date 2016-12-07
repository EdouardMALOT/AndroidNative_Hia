package io.hia.hia;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Types.BoomType;
import com.nightonke.boommenu.Types.ButtonType;
import com.nightonke.boommenu.Types.PlaceType;
import com.nightonke.boommenu.Util;

import java.util.Random;

/**
 * Created by edouard on 04/10/16.
 */

class ContactRowAdapter extends ArrayAdapter<String>{

    //////////////////////////////////////////////////
    //                                              //
    //                 Constructor                  //
    //                                              //
    //////////////////////////////////////////////////
    ContactRowAdapter(Context context, String[] Names) {
        super(context, R.layout.contact_row, Names);
    }

    //////////////////////////////////////////////////
    //                                              //
    //                ViewHolder                    //
    //                                              //
    //////////////////////////////////////////////////
    class ViewHolder {
        public TextView tv;
        public BoomMenuButton circleBoomMenuButton;
    }

    //////////////////////////////////////////////////
    //                                              //
    //                  getView                     //
    //                                              //
    //////////////////////////////////////////////////
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder viewHolder;

        //Create View
        //------------
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_row, null);

                    viewHolder = new ViewHolder();

                    viewHolder.tv = (TextView) convertView.findViewById(R.id.NameView);
                    viewHolder.circleBoomMenuButton = (BoomMenuButton) convertView.findViewById(R.id.boom_circle);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tv.setText(getItem(position));


        //Configure Boom circles
        //----------------------
            //Set Icons
            int[] drawablesResource = new int[]{ R.drawable.question, R.drawable.position_bp, R.drawable.unepensee };
            final Drawable[] circleSubButtonDrawables = new Drawable[3];

            for (int i = 0; i < 3; i++)  circleSubButtonDrawables[i] = ContextCompat.getDrawable(parent.getContext(), drawablesResource[i]);

            //Set Texts
            final String[] circleSubButtonTexts = new String[]{ "T'es où ?", "J'suis là !", "Une pensée !"};
            final int[][] subButtonColors = new int[3][2];

            for (int i = 0; i < 3; i++) {
                subButtonColors[i][1] = GetRandomColor();
                subButtonColors[i][0] = Util.getInstance().getPressedColor(subButtonColors[i][1]);
            }


        // Init the BMB with delay
        //------------------------
        viewHolder.circleBoomMenuButton.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // Now with Builder, you can init BMB more convenient
                    new BoomMenuButton.Builder()
                            .subButtons(circleSubButtonDrawables, subButtonColors, circleSubButtonTexts)
                            .button(ButtonType.CIRCLE)
                            .boom(BoomType.PARABOLA)
                            .place(PlaceType.CIRCLE_3_1)
                            .subButtonsShadow(Util.getInstance().dp2px(2), Util.getInstance().dp2px(2))

                            .onSubButtonClick(new BoomMenuButton.OnSubButtonClickListener() {

                                @Override
                                public void onClick(int buttonIndex) {


                                    String NameofMyFriend = ShowContacts.NameList.get(position);
                                    String NumberOfMyFriend = ShowContacts.NumberList.get(position);

                                    SendToServer  nSendToServer = new SendToServer();

                                    switch (buttonIndex) {

                                        case 0:
                                            //"Tu es où ?"
                                            //------------
                                                nSendToServer.Send(parent.getContext(), ShowContacts.GetMyPhoneNumber(), NameofMyFriend, NumberOfMyFriend, "HiaQuestion");
                                            break;

                                        case 1:
                                             //"Je suis là"
                                             //------------
                                                    nSendToServer.Send(parent.getContext(), ShowContacts.GetMyPhoneNumber(), NameofMyFriend, NumberOfMyFriend, "Hia");
                                            break;
                                        case 2:
                                            //"Une pensée"
                                            //------------
                                                    nSendToServer.Send(parent.getContext(), ShowContacts.GetMyPhoneNumber(), NameofMyFriend, NumberOfMyFriend, "Thought");
                                            break;

                                        default:
                                                    Toast.makeText( parent.getContext(), "On click " + circleSubButtonTexts[buttonIndex],  Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }

                            })
                            .duration(300)
                            .init(viewHolder.circleBoomMenuButton);
                }

        }, 1);

        return convertView;
    }



    //////////////////////////////////////////////////
    //                                              //
    //               GetRandomColor                 //
    //                                              //
    //////////////////////////////////////////////////
    private static String[] Colors = {  "#F44336", "#E91E63", "#9C27B0", "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722", "#795548", "#9E9E9E", "#607D8B"};

    public static int GetRandomColor() {
        Random random = new Random();
        int p = random.nextInt(Colors.length);
        return Color.parseColor(Colors[p]);
    }

}
