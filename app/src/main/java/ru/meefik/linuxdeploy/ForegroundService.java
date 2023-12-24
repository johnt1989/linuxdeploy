package ru.meefik.linuxdeploy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import ru.meefik.linuxdeploy.activity.MainActivity;

import static androidx.core.app.NotificationCompat.FLAG_NO_CLEAR;
import static androidx.core.app.NotificationCompat.FLAG_ONGOING_EVENT;

/**
 * Created by cdb on 2023-11-23.
 */
public class ForegroundService extends Service {
    public static final String ID = "0x110088";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotification(getString(R.string.app_name),
                getString(R.string.notification_current_profile) + ": " + PrefStore.getProfileName(this));
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * @param context
     * @return 兼容高版本o以上
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification createCompatibleNotification(Context context, String title, String content) {
        NotificationChannel chan = new NotificationChannel(ID, context.getResources().getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        Intent intentChart = new Intent(this, MainActivity.class);

        intentChart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intentChart, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(context, ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_SERVICE).build();
    }

    /**
     * @param context
     * @return 兼容低版本
     */
    private Notification createMainNotification(Context context, String title, String content) {
        Intent intentChart = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentChart, 0);
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        builder.setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_HIGH);
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= FLAG_ONGOING_EVENT;
        notification.flags |= FLAG_NO_CLEAR;
        return notification;
    }

    protected void showNotification(String title, String content) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForeground(101, createCompatibleNotification(this, title, content));
            } else {
                startForeground(101, createMainNotification(this, title, content));
            }
            Log.i(ExecService.TAG, "showNotification");
        } catch (Exception e) {
            Log.e(ExecService.TAG, "showNotification error: " + Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    protected void dismissNotification() {
        try {
            stopForeground(true);
        } catch (Exception e) {
        }
        Log.i(ExecService.TAG, "dismissNotification");
    }

    @Override
    public void onDestroy() {
        dismissNotification();
    }
}
