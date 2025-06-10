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


public class MishnaYomitService extends Service {
    private static final int NOTIFICATION_ID = 4;
    private static final String CHANNEL_ID = "mishna_yomit_channel";
    private static final String API_URL = "https://www.sefaria.org/api/calendars";

    /**
     * Called when the service is created.
     * Creates the notification channel if required (Android O and above).
     */
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Called when the service is started.
     * Starts the service in the foreground with an initial notification and fetches the daily Mishna.
     *
     * @param intent  The Intent that started the service
     * @param flags   Additional data about the start request
     * @param startId A unique integer representing this specific request to start
     * @return START_STICKY to indicate that the system should try to recreate the service after it is killed
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, getMishnaYomitNotification("טוען..."));
        fetchMishnaYomitFromAPI();
        return START_STICKY;
    }

    /**
     * Performs an HTTP request to fetch the daily Mishna calendar from the API.
     * Parses the JSON response and updates the notification with today's Mishna.
     */
    private void fetchMishnaYomitFromAPI() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MishnaYomitService", "API request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String dafYomi = jsonObject.getJSONArray("calendar_items")
                                .getJSONObject(4)
                                .getJSONObject("displayValue")
                                .getString("he");
                        updateNotification(dafYomi);
                    } catch (Exception e) {
                        Log.e("MishnaYomitService", "JSON parsing error", e);
                    }
                }
            }
        });
    }

    /**
     * Updates the existing notification with the provided daily Mishna text.
     *
     * @param dailyPage The text of today's daily Mishna
     */
    private void updateNotification(String dailyPage) {
        Notification notification = getMishnaYomitNotification(dailyPage);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Builds and returns a Notification object to display in the foreground service.
     *
     * @param dailyPage The text to display in the notification
     * @return A Notification object showing the daily Mishna
     */
    private Notification getMishnaYomitNotification(String dailyPage) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("המשנה היומית להיום")
                .setContentText(dailyPage)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    /**
     * Creates the notification channel for Android O and above.
     * The channel defines the importance and behavior of notifications.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Mishna Yomit Notifications",
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
     * @param intent The intent that was used to bind to this service
     * @return Always returns null since binding is not allowed
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
