package com.lucasasselli.evernotewear.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.lucasasselli.evernotewear.CommManager;
import com.lucasasselli.evernotewear.MainActivity;
import com.lucasasselli.evernotewear.MyEvernote;
import com.lucasasselli.evernotewear.R;
import com.lucasasselli.evernotewear.Utils;

public class SyncManager extends Service {

    private static final String TAG = "SyncManager";
    private int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    int WAKE_UP_DELAY;

    // Broadcasts
    public final static String BROADCAST_SYNC_COMPLETED = "BROADCAST_SYNC_COMPLETED";

    // Extras
    public final static String EXTRA_RESULT = "EXTRA_RESULT";

    // Results
    public final static String RESULT_SUCCESS = "RESULT_SUCCESSFUL";
    public final static String RESULT_FAILED = "RESULT_FAILED";

    public static void start(Context context){
        context.startService(new Intent(context, SyncManager.class));
    }

    public SyncManager() {
    }

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        // Retrieve sync frequency amount and wifi only prefs
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        int syncFrequncyPref = Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_sync_frequency_key), getString(R.string.pref_sync_frequency_default)));
        WAKE_UP_DELAY = 60 * syncFrequncyPref * 1000;

        boolean wifiOnlyPref = sharedPreferences.getBoolean(getString(R.string.pref_wifi_only_key), getResources().getBoolean(R.bool.pref_wifi_only_default));
        boolean showNotificationPref = sharedPreferences.getBoolean(getString(R.string.pref_show_notifications_key), getResources().getBoolean(R.bool.pref_show_notifications_default));

        int connectionType = Utils.getInternetConnection(this);

        if(connectionType!=Utils.NO_CONNECTION){
            if((wifiOnlyPref && connectionType==Utils.WIFI) || (!wifiOnlyPref)) {
                // SyncAllTask handles notifications and service stop
                SyncAllTask syncAllTask = new SyncAllTask(this, showNotificationPref);
                syncAllTask.execute();
            }else{
                // End service
                Log.e(TAG, "Sync is possible only over wifi connection");
                stopSelf();
            }
        }else{
            // End service
            Log.e(TAG, "No internet connection");
            stopSelf();
        }


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // I want to restart this service again
        if(WAKE_UP_DELAY!=0) {
            Log.d(TAG, "Service tasks completed! Setting alarm in " + WAKE_UP_DELAY + " millis");

            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, SyncManager.class), 0);
            alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + WAKE_UP_DELAY, pendingIntent);
        }else{
            Log.d(TAG, "Sync frequency is 0: no alarm will be set");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void showNotification(boolean result, String title, String text) {

        PendingIntent startApp = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        PendingIntent startService = PendingIntent.getService(this, 0, new Intent(this, SyncManager.class), 0);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setSmallIcon(R.drawable.ic_notification_small);
        builder.setColor(getResources().getColor(R.color.accent));
        builder.setTicker(text);
        builder.setContentTitle(title);
        builder.setContentText(text);

        if(result){
            builder.setWhen(System.currentTimeMillis());
            builder.setContentIntent(startService);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_warning_notification));
        }else{
            builder.setProgress(0,0, true);
            builder.setContentIntent(startApp);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_sync_notification));
        }

        Notification notification = builder.build();

        // Send the notification.
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public class SyncAllTask extends AsyncTask<Void, Void, Integer> {

        Context context;
        boolean showNotification;

        SyncAllTask(Context context, boolean showNotification){
            this.context = context;
            this.showNotification = showNotification;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(showNotification) {
                showNotification(false, getString(R.string.notification_sync_running_title), getString(R.string.notification_sync_running));
            }
        }

        @Override
        protected Integer doInBackground(Void ... voids) {
            MyEvernote myEvernote = new MyEvernote(context);
            return myEvernote.syncAll();
        }

        @Override
        protected void onPostExecute(Integer result) {
            Intent intent = new Intent(BROADCAST_SYNC_COMPLETED);

            if (result == MyEvernote.OK) {
                Log.d(TAG, "Autosync succesful");
                intent.putExtra(EXTRA_RESULT, RESULT_SUCCESS);
                notificationManager.cancel(NOTIFICATION_ID);
            } else {
                Log.d(TAG, "Autosync failed");
                if(showNotification) {
                    intent.putExtra(EXTRA_RESULT, RESULT_FAILED);
                    showNotification(true, getString(R.string.notification_failed_title), getString(R.string.notification_failed));
                }
            }

            // Send broadcast
            sendBroadcast(intent);

            // Send the updated database
            CommManager commManager = new CommManager(context);
            commManager.sendDatabase();

            // Whatever happens, kill service
            stopService(new Intent(context, SyncManager.class));
        }

    }

    public static class AutoStart extends BroadcastReceiver {

        public AutoStart(){

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Boot completed, starting service...");
            context.startService(new Intent(context, SyncManager.class));
        }
    }
}
