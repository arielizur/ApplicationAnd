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
 * A foreground Android Service that displays a daily notification
 * with the "Tanach Yomi" (Daily Tanach Study) from the Sefaria API.
 */
public class TanahYomyService extends Service {

    /** ID for the Tanach Yomi notification shown in the foreground. */
    private static final int NOTIFICATION_ID = 3;

    /** ID for the notification channel used on Android O and above. */
    private static final String CHANNEL_ID = "tanah_yomy_channel";

    /** URL of the Sefaria API used to fetch the daily study items. */
    private static final String API_URL = "https://www.sefaria.org/api/calendars";

    /**
     * Called when the service is first created.
     * Creates a notification channel if required by the OS version.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Called when the service is started.
     * Starts the service in the foreground with a loading notification,
     * then fetches the actual daily study from the API.
     *
     * @param intent  The intent used to start the service.
     * @param flags   Additional data on how the service was started.
     * @param startId A unique integer ID for this specific request to start.
     * @return START_STICKY to request system restart if the service is killed.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, getTanahYomyNotification("טוען..."));
        fetchTanahYomiFromAPI();
        return START_STICKY;
    }

    /**
     * Sends an asynchronous request to the Sefaria API to retrieve the current day's
     * "Tanach Yomi" study text, and updates the notification with it.
     */
    private void fetchTanahYomiFromAPI() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TanahYomyService", "API request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String tanahYomi = jsonObject.getJSONArray("calendar_items")
                                .getJSONObject(3)
                                .getJSONObject("displayValue")
                                .getString("he");

                        updateNotification(tanahYomi);
                    } catch (Exception e) {
                        Log.e("TanahYomyService", "JSON parsing error", e);
                    }
                }
            }
        });
    }

    /**
     * Updates the currently displayed notification with the actual daily Tanach study.
     *
     * @param dailyPage The Hebrew text representing today's Tanach Yomi item.
     */
    private void updateNotification(String dailyPage) {
        Notification notification = getTanahYomyNotification(dailyPage);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Builds and returns a notification with the given text.
     *
     * @param dailyPage The daily Tanach study text to be shown.
     * @return A configured Notification object.
     */
    private Notification getTanahYomyNotification(String dailyPage) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("התנ”ך היומי להיום")
                .setContentText(dailyPage)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    /**
     * Creates the notification channel required for Android O and above.
     * This must be called before showing any notifications on those versions.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tanach Yomi Notifications",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * This service does not support binding, so this always returns null.
     *
     * @param intent The intent used to bind to the service.
     * @return Always null since binding is not supported.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
