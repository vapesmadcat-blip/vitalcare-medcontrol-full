package com.vitalcare.medcontrol;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "vitalcare_alarm_channel";
    private static final String CHANNEL_SILENT_ID = "vitalcare_silent_channel";

    private void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel alarm = new NotificationChannel(CHANNEL_ID, "Alarmes VitalCare", NotificationManager.IMPORTANCE_HIGH);
            alarm.enableVibration(true);
            alarm.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null);

            NotificationChannel silent = new NotificationChannel(CHANNEL_SILENT_ID, "Alertas silenciosos VitalCare", NotificationManager.IMPORTANCE_HIGH);
            silent.enableVibration(false);
            silent.setSound(null, null);

            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(alarm);
                nm.createNotificationChannel(silent);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String med = intent.getStringExtra("med");
        String dose = intent.getStringExtra("dose");
        String mode = intent.getStringExtra("mode");
        int id = intent.getIntExtra("id", (int)(System.currentTimeMillis() % 100000));

        if (med == null) med = "Medicamento";
        if (dose == null) dose = "";
        if (mode == null) mode = "due";

        ensureChannels(context);

        boolean silent = mode.contains("silent");
        boolean full = mode.equals("due") || mode.equals("due_silent");

        String title;
        String body;
        if (mode.startsWith("pre")) {
            title = "⏰ Dose em 5 minutos";
            body = med + " " + dose;
        } else if (mode.startsWith("post")) {
            title = "⚠️ Dose pendente";
            body = med + " " + dose + " · passou 5 minutos";
        } else {
            title = "💊 Hora da dose";
            body = med + " " + dose;
        }

        Intent fullIntent = new Intent(context, AlarmActivity.class);
        fullIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fullIntent.putExtra("med", med);
        fullIntent.putExtra("dose", dose);
        fullIntent.putExtra("mode", mode);

        PendingIntent fullPi = PendingIntent.getActivity(
                context,
                id + 700000,
                fullIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String channel = silent ? CHANNEL_SILENT_ID : CHANNEL_ID;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(fullPi)
                .setAutoCancel(true);

        if (silent) {
            builder.setSilent(true).setVibrate(new long[]{0});
        } else {
            builder.setSound(RingtoneManager.getDefaultUri(full ? RingtoneManager.TYPE_ALARM : RingtoneManager.TYPE_NOTIFICATION))
                   .setVibrate(new long[]{0,1000,500,1000,500,1000});
        }

        if (full) {
            builder.setFullScreenIntent(fullPi, true);
        }

        if (Build.VERSION.SDK_INT < 33 || ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(id, builder.build());
        }

        if (full) {
            context.startActivity(fullIntent);
        }
    }
}
