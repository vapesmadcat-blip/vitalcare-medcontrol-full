package com.vitalcare.medcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "MedControlBoot";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(TAG, "Boot completed - alarms may need rescheduling by opening the app");
            // TODO: For full support, persist scheduled regs in native SharedPreferences 
            // and reschedule here using AlarmScheduler, or flag for MainActivity to re-arm on next launch.
            // Current implementation relies on JS logic + localStorage, so open the app after reboot.
        }
    }
}
