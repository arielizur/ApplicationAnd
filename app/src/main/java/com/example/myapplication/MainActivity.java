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
import androidx.annotation.NonNull;
import android.content.pm.PackageManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.CheckBox;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private Switch switchDafYomy, switchRambamYomy, switchTanahYomy, switchMishnaYomit;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPrefs";
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        switchDafYomy = findViewById(R.id.switch2);
        switchRambamYomy = findViewById(R.id.switch3);
        switchTanahYomy = findViewById(R.id.switch7);
        switchMishnaYomit = findViewById(R.id.switch8);

        initSwitchState(switchDafYomy, "dafYomiSwitchState", DafYomyService.class);
        initSwitchState(switchRambamYomy, "rambamYomiSwitchState", RambamYomyService.class);
        initSwitchState(switchTanahYomy, "tanahYomiSwitchState", TanahYomyService.class);
        initSwitchState(switchMishnaYomit, "mishnaYomitSwitchState", MishnaYomitService.class);

        checkBox1 = findViewById(R.id.checkBox1);
        checkBox2 = findViewById(R.id.checkBox2);
        checkBox3 = findViewById(R.id.checkBox3);
        checkBox4 = findViewById(R.id.checkBox4);

        initCheckBox(checkBox1, "checkbox1_state", "checkbox1_last_update");
        initCheckBox(checkBox2, "checkbox2_state", "checkbox2_last_update");
        initCheckBox(checkBox3, "checkbox3_state", "checkbox3_last_update");
        initCheckBox(checkBox4, "checkbox4_state", "checkbox4_last_update");



        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotebook(DafYomyNotebook.class);
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotebook(RambamYomyNoatebook.class);
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotebook(TanahYomyNoatebook.class);
            }
        });

        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotebook(MishnaYomitNoatebook.class);
            }
        });

        findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });

        boolean isFirstLaunch = sharedPreferences.getBoolean("firstLaunch", true);
        if (isFirstLaunch) {
            requestNotificationPermission();
            sharedPreferences.edit().putBoolean("firstLaunch", false).apply();
        }

        ImageButton buttonAboutApp = findViewById(R.id.imageButton2);
        buttonAboutApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        });

        ImageButton imageButton5 = findViewById(R.id.imageButton5);
        imageButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showAboutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("אודות האפליקציה")
                .setMessage("זוהי אפליקציה ללימוד יומי של דף יומי, רמב״ם יומי, תנ״ך יומי ומשנה יומית.\n\n" +
                        "באפשרותך להפעיל שירותי התראה לכל מסלול לימוד ולשמור הערות שונות לכל לימוד בלחיצה על הכפתור 'הערות'.\n\n" +
                        "מידע חשוב! לחיצה על כפתור 'שחזור גיבוי' תחליף את הטקסט הקיים בטקסט שגובה בעבר (אם לא גובה טקסט, הטקסט הקיים ימחק) ולחיצה על 'גיבוי לענן' תחליף את הגיבוי הקיים בגיבוי חדש")
                .setPositiveButton("אישור", null)
                .show();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "הרשאת התרעות אושרה");
            } else {
                Log.d("MainActivity", "הרשאת התרעות נדחתה");
            }
        }
    }

    private void initCheckBox(CheckBox checkBox, String stateKey, String dateKey) {
        resetCheckBoxIfNeeded(stateKey, dateKey);

        boolean isChecked = sharedPreferences.getBoolean(stateKey, false);
        checkBox.setChecked(isChecked);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            sharedPreferences.edit()
                    .putBoolean(stateKey, isChecked1)
                    .putLong(dateKey, System.currentTimeMillis())
                    .apply();
        });
    }

    private void resetCheckBoxIfNeeded(String stateKey, String dateKey) {
        long lastUpdate = sharedPreferences.getLong(dateKey, 0);
        Calendar lastCal = Calendar.getInstance();
        lastCal.setTimeInMillis(lastUpdate);

        Calendar now = Calendar.getInstance();

        boolean isNewDay =
                now.get(Calendar.YEAR) != lastCal.get(Calendar.YEAR) ||
                        now.get(Calendar.DAY_OF_YEAR) != lastCal.get(Calendar.DAY_OF_YEAR);

        if (isNewDay) {
            sharedPreferences.edit().putBoolean(stateKey, false).apply();
        }
    }



    private void initSwitchState(Switch switchButton, final String key, final Class<?> serviceClass) {
        boolean isSwitchOn = sharedPreferences.getBoolean(key, false);
        switchButton.setChecked(isSwitchOn);
        final Intent serviceIntent = new Intent(this, serviceClass);

        if (isSwitchOn) {
            ContextCompat.startForegroundService(this, serviceIntent);
        }

        switchButton.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean(key, isChecked).apply();
                if (isChecked) {
                    Log.d("MainActivity", key + " turned ON - starting service");
                    ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
                } else {
                    Log.d("MainActivity", key + " turned OFF - stopping service");
                    stopService(serviceIntent);
                }
            }
        });
    }

    private void openNotebook(Class<?> notebookClass) {
        startActivity(new Intent(MainActivity.this, notebookClass));
    }

    private void showPopupWindow(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_layout, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupView.findViewById(R.id.button_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/972506304230?text=אשמח+לקבל+עזרה")));
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.button_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:0506304230")));
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupWindow.showAsDropDown(view, 0, 0, Gravity.CENTER);
    }
}
