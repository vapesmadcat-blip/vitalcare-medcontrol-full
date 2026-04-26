package com.vitalcare.medcontrol;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmScheduler {
    public static void schedule(Context context, int id, long timeMillis, String med, String dose) {
        schedule(context, id, timeMillis, med, dose, "due");
    }

    public static void schedule(Context context, int id, long timeMillis, String med, String dose, String mode) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("med", med);
        intent.putExtra("dose", dose);
        intent.putExtra("id", id);
        intent.putExtra("mode", mode == null ? "due" : mode);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pi);
        }
    }

    public static void cancel(Context context, int id) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(pi);
    }
}
