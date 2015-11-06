package com.example.admin.route;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by derek on 6/11/2015.
 */
public class BatteryLevelReceiver extends BroadcastReceiver{
    public void onReceive(Context ctx, Intent intent ) {
        String intentAction = intent.getAction();

        if(Intent.ACTION_BATTERY_LOW.equalsIgnoreCase(intentAction)){
            Toast.makeText(ctx, "Low Battery detected. You may want to turn off Route Diary to save battery.",
                    Toast.LENGTH_SHORT).show();
        }

        else if(Intent.ACTION_BATTERY_OKAY.equalsIgnoreCase(intentAction)){
            Toast.makeText(ctx, "Battery is fine, continue to use Route Diary to serve your needs.",
                    Toast.LENGTH_SHORT).show();
        }

        else if(Intent.ACTION_POWER_CONNECTED.equalsIgnoreCase(intentAction)){
            Toast.makeText(ctx, "Connected to Power Supply.",
                    Toast.LENGTH_SHORT).show();
        }

        else if(Intent.ACTION_POWER_DISCONNECTED.equalsIgnoreCase(intentAction)){
            Toast.makeText(ctx, "Disconnected from Power Supply.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
