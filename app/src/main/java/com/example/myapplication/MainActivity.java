package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Switch switchDafYomy, switchRambamYomy, switchTanahYomy, switchMishnaYomit;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // אתחול Switchים
        switchDafYomy = findViewById(R.id.switch2);
        switchRambamYomy = findViewById(R.id.switch3);
        switchTanahYomy = findViewById(R.id.switch7);
        switchMishnaYomit = findViewById(R.id.switch8);

        // הגדרת מצבים מהעבר
        initSwitchState(switchDafYomy, "dafYomiSwitchState", DafYomyService.class);
        initSwitchState(switchRambamYomy, "rambamYomiSwitchState", RambamYomyService.class);
        initSwitchState(switchTanahYomy, "tanahYomiSwitchState", TanahYomyService.class);
        initSwitchState(switchMishnaYomit, "mishnaYomitSwitchState", MishnaYomitService.class);

        // כפתורים לפתיחת פנקסים
        findViewById(R.id.button2).setOnClickListener(v -> openNotebook(DafYomyNotebook.class));
        findViewById(R.id.button3).setOnClickListener(v -> openNotebook(RambamYomyNoatebook.class));
        findViewById(R.id.button4).setOnClickListener(v -> openNotebook(TanahYomyNoatebook.class));
        findViewById(R.id.button5).setOnClickListener(v -> openNotebook(MishnaYomitNoatebook.class));

        // כפתור לפתיחת תפריט
        findViewById(R.id.imageButton).setOnClickListener(this::showPopupWindow);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // פונקציה לאתחול מצב Switch
    private void initSwitchState(Switch switchButton, String key, Class<?> serviceClass) {
        boolean isSwitchOn = sharedPreferences.getBoolean(key, false);
        switchButton.setChecked(isSwitchOn);
        Intent serviceIntent = new Intent(this, serviceClass);

        if (isSwitchOn) {
            ContextCompat.startForegroundService(this, serviceIntent);
        }

        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(key, isChecked).apply();
            if (isChecked) {
                Log.d("MainActivity", key + " turned ON - starting service");
                ContextCompat.startForegroundService(this, serviceIntent);
            } else {
                Log.d("MainActivity", key + " turned OFF - stopping service");
                stopService(serviceIntent);
            }
        });
    }

    // פונקציה לפתיחת Notebook
    private void openNotebook(Class<?> notebookClass) {
        startActivity(new Intent(MainActivity.this, notebookClass));
    }

    // תפריט PopUp
    private void showPopupWindow(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_layout, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupView.findViewById(R.id.button_settings).setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/972506304230?text=אשמח+לקבל+עזרה")));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.button_about).setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:0506304230")));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.button_close).setOnClickListener(v -> popupWindow.dismiss());

        popupWindow.showAsDropDown(view, 0, 0, Gravity.CENTER);
    }
}
