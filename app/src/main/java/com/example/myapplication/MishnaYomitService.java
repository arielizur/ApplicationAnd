package com.example.myapplication;

import android.app.*;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class MishnaYomitService extends Service {
    private static final int NOTIFICATION_ID = 4;
    private static final String CHANNEL_ID = "mishna_yomit_channel";
    private static final String API_URL = "https://www.sefaria.org/api/calendars";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, getMishnaYomitNotification("טוען..."));
        fetchMishnaYomitFromAPI();
        return START_STICKY;
    }

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

    private void updateNotification(String dailyPage) {
        Notification notification = getMishnaYomitNotification(dailyPage);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, notification);
    }

    private Notification getMishnaYomitNotification(String dailyPage) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("המשנה היומית להיום")
                .setContentText(dailyPage)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
