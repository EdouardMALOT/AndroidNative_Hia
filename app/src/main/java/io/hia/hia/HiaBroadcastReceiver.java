package io.hia.hia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HiaBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myIntent = new Intent(context, HiaBackGroundService.class);
        context.startService(myIntent);
    }
}
