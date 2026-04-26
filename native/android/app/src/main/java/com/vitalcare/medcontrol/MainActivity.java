package com.vitalcare.medcontrol;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private static Ringtone ringtone;
    private static final String CHANNEL_ID = "vitalcare_alarm_channel";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createChannel();
        this.bridge.getWebView().addJavascriptInterface(new JSBridge(this), "Android");
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarmes VitalCare",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Testes nativos de alarme e medicação");
            channel.enableVibration(true);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public static class JSBridge {
        private final MainActivity activity;
        JSBridge(MainActivity activity) { this.activity = activity; }

        @JavascriptInterface
        public String diagnose() {
            boolean notifyGranted = Build.VERSION.SDK_INT < 33 || ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
            boolean exact = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
                exact = am != null && am.canScheduleExactAlarms();
            }
            return "Ponte OK | Notificação=" + notifyGranted + " | AlarmeExato=" + exact;
        }

        @JavascriptInterface
        public String requestNotificationPermission() {
            if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                activity.runOnUiThread(() -> ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001));
                return "Solicitação de permissão enviada";
            }
            return "Permissão já concedida ou não necessária";
        }

        @JavascriptInterface
        public String openExactAlarmSettings() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
                return "Tela de alarme exato aberta";
            }
            return "Alarme exato não requer configuração nesta versão";
        }

        @JavascriptInterface
        public String showSimpleNotification(String title, String body) {
            activity.createChannel();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{0, 500, 250, 500})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            if (Build.VERSION.SDK_INT < 33 || ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(activity).notify((int)(System.currentTimeMillis() % 100000), builder.build());
                return "Notificação enviada";
            }
            return "Sem permissão POST_NOTIFICATIONS";
        }

        @JavascriptInterface
        public String playAlarmSound() {
            try {
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
                ringtone = RingtoneManager.getRingtone(activity, uri);
                ringtone.play();
                return "Som iniciado";
            } catch (Exception e) {
                return "Erro som: " + e.getMessage();
            }
        }

        @JavascriptInterface
        public String stopAlarmSound() {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
                return "Som parado";
            }
            return "Nenhum som tocando";
        }

        @JavascriptInterface
        public String openFullScreen(String med, String dose) {
            Intent intent = new Intent(activity, AlarmActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("med", med);
            intent.putExtra("dose", dose);
            activity.startActivity(intent);
            return "Tela cheia aberta";
        }

        @JavascriptInterface
        public String scheduleAlarm(int id, long timeMillis, String med, String dose) {
            AlarmScheduler.schedule(activity, id, timeMillis, med, dose, "due");
            return "Alarme agendado id=" + id + " time=" + timeMillis;
        }

        @JavascriptInterface
        public String scheduleTypedAlarm(int id, long timeMillis, String med, String dose, String mode) {
            AlarmScheduler.schedule(activity, id, timeMillis, med, dose, mode);
            return "Alarme agendado id=" + id + " mode=" + mode + " time=" + timeMillis;
        }

        @JavascriptInterface
        public String cancelAlarm(int id) {
            AlarmScheduler.cancel(activity, id);
            return "Alarme cancelado id=" + id;
        }
    }
}
