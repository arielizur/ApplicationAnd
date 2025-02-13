public class DafYomyService {
import android.app.*;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class DafYomyService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "daf_yomy_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getDafyomyNotification("טוען..."));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String dailyPage = getDailyPage(); // נשלוף את הדף היומי
        Notification notification = getDafYomyNotification(dailyPage);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    private Notification getDafyomyNotification(String dailyPage) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("הדף היומי להיום")
                .setContentText(dailyPage)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true) // ההתראה לא ניתנת להסרה
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Daf Yomy Notifications",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private String getDailyPage() {
        // כאן תשלב קריאה ל-API כדי לקבל את הדף היומי
        return "ברכות דף ב"; // כרגע זה נתון מדומה
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
{
}
        }