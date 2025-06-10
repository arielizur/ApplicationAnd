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

/**
 * A foreground Android Service that displays the daily Rambam study notification.
 * It fetches the daily Rambam study from the Sefaria API and updates the notification with the result.
 */
public class RambamYomyService extends Service {

    /** ID for the notification shown in the foreground. */
    private static final int NOTIFICATION_ID = 2;

    /** ID for the notification channel. */
    private static final String CHANNEL_ID = "Rambam_yomy_channel";

    /** URL of the Sefaria API for daily calendar items. */
    private static final String API_URL = "https://www.sefaria.org/api/calendars";

    /**
     * Called when the service is first created.
     * Sets up the notification channel for Android O and above.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Called when the service is started.
     * Begins the service in the foreground and fetches the daily Rambam study.
     *
     * @param intent  The Intent that was used to start the service.
     * @param flags   Additional data about this start request.
     * @param startId A unique integer identifying this specific request to start.
     * @return START_STICKY to indicate that the system should restart the service if it is killed.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, getRambamYomyNotification("טוען..."));
        fetchRambamYomiFromAPI();
        return START_STICKY;
    }

    /**
     * Performs a network request to retrieve the daily Rambam study from the Sefaria API.
     * On success, updates the notification with the study item.
     */
    private void fetchRambamYomiFromAPI() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("RambamYomyService", "API request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String rambamYomi = jsonObject.getJSONArray("calendar_items")
                                .getJSONObject(5)
                                .getJSONObject("displayValue")
                                .getString("he");

                        updateNotification(rambamYomi);
                    } catch (Exception e) {
                        Log.e("RambamYomyService", "JSON parsing error", e);
                    }
                }
            }
        });
    }

    /**
     * Updates the existing notification with the given daily Rambam study text.
     *
     * @param dailyPage The daily Rambam study item to display.
     */
    private void updateNotification(String dailyPage) {
        Notification notification = getRambamYomyNotification(dailyPage);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Builds and returns a Notification object with the given text.
     *
     * @param dailyPage The text to display in the notification.
     * @return The constructed Notification.
     */
    private Notification getRambamYomyNotification(String dailyPage) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("הרמב”ם היומי להיום")
                .setContentText(dailyPage)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    /**
     * Creates a notification channel for devices running Android O and above.
     * This is required for displaying notifications on newer Android versions.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Rambam Yomy Notifications",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Binding is not supported for this service.
     *
     * @param intent The intent used to bind to the service.
     * @return Always returns null as this service does not support binding.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
