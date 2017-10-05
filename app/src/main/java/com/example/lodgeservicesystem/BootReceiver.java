package com.example.lodgeservicesystem;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.lodgeservicesystem.temp.MQTTService;

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(getClass().getCanonicalName(), "onReceive");
        context.startService(new Intent(context, MQTTService.class));
    }
}