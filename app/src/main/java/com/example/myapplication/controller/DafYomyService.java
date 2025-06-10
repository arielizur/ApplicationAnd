package com.example.myapplication.controller;

import android.app.*;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Calendar;

/**
 * A foreground Android service that displays the Daf Yomi (daily Talmud page)
 * in a persistent notification and updates it daily at midnight.
 */
public class DafYomyService extends Service {

    /** Notification ID for the foreground service */
    private static final int NOTIFICATION_ID = 1;

    /** Notification channel ID for Android O and above */
    private static final String CHANNEL_ID = "daf_yomy_channel";

    /** Sefaria API URL to fetch calendar and Daf Yomi data */
    private static final String API_URL = "https://www.sefaria.org/api/calendars";

    /**
     * Called when the service is first created.
     * Initializes the notification channel.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Called every time the service is started.
     * Starts the service in the foreground and initiates data fetching from the API.
     *
     * @param intent  The Intent supplied to startService(Intent).
     * @param flags   Additional data about the start request.
     * @param startId A unique integer representing this specific request to start.
     * @return START_STICKY to keep the service running.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, getDafYomyNotification("Loading..."));
        fetchDafYomiFromAPI();
        return START_STICKY;
    }

    /**
     * Sends an HTTP request to the Sefaria API to fetch today's Daf Yomi.
     * On success, it updates the notification and schedules the next update.
     */
    private void fetchDafYomiFromAPI() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("DafYomyService", "API request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String dafYomi = jsonObject.getJSONArray("calendar_items")
                                .getJSONObject(2)
                                .getJSONObject("displayValue")
                                .getString("he");

                        updateNotification(dafYomi);
                    } catch (Exception e) {
                        Log.e("DafYomyService", "JSON parsing error", e);
                    }
                }
            }
        });
    }

    /**
     * Updates the ongoing notification with the new Daf Yomi value
     * and schedules the next daily update.
     *
     * @param dailyPage The Daf Yomi page title in Hebrew.
     */
    private void updateNotification(String dailyPage) {
        Notification notification = getDafYomyNotification(dailyPage);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, notification);

        scheduleNextUpdate();
    }

    /**
     * Builds and returns the notification displaying the Daf Yomi.
     *
     * @param dailyPage The text to display in the notification.
     * @return A Notification object for use with startForeground or notify.
     */
    private Notification getDafYomyNotification(String dailyPage) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Today's Daf Yomi")
                .setContentText(dailyPage)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    /**
     * Creates the notification channel required for Android 8.0+.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daf Yomi Notifications",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Schedules the next daily update using AlarmManager to run at midnight.
     */
    private void scheduleNextUpdate() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, DafYomyService.class);
        PendingIntent pendingIntente = PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntente
        );

        Log.d("DafYomyService", "Next update scheduled for: " + calendar.getTime());
    }

    /**
     * This service does not support binding.
     *
     * @param intent The Intent that was used to bind to this service.
     * @return Always returns null since binding is not supported.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
