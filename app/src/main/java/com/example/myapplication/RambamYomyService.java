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

public class RambamYomyService extends Service {
    private static final int NOTIFICATION_ID = 2;
    private static final String CHANNEL_ID = "Rambam_yomy_channel";
    private static final String API_URL = "https://www.sefaria.org/api/calendars"; // עדכן לפי ה-API שלך

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, getRambamYomyNotification("טוען..."));
        fetchRambamYomiFromAPI();
        return START_STICKY;
    }

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
                                .getJSONObject(6)
                                .getJSONObject("displayValue") // קבלת האובייקט
                                .getString("he"); // שליפת הדף היומי בעברית

                        updateNotification(rambamYomi);
                    } catch (Exception e) {
                        Log.e("RambamYomyService", "JSON parsing error", e);
                    }
                }
            }
        });
    }

    private void updateNotification(String dailyPage) {
        Notification notification = getRambamYomyNotification(dailyPage);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, notification);
    }

    private Notification getRambamYomyNotification(String dailyPage) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("הרמב''ם היומי להיום")
                .setContentText(dailyPage)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Rambam Yomy Notifications",
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
