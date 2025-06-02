package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Calendar;

/**
 * The main activity of the application, responsible for displaying the main user interface,
 * handling user interactions with switches and checkboxes, managing shared preferences,
 * and navigating to different parts of the application like notebooks and login.
 * It also handles notification permission requests and displays an "About" dialog.
 */
public class MainActivity extends AppCompatActivity {

    private Switch switchDafYomy, switchRambamYomy, switchTanahYomy, switchMishnaYomit;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPrefs";
    private CheckBox checkBoxDafYomy, checkBoxRambamYomy, checkBoxTanahYomy, checkBoxMishnaYomit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initEdgeToEdgeUI();
        initSharedPreferences();
        initSwitches();
        initCheckBoxes();
        initNotebookButtons();
        initPopupMenu();
        handleFirstLaunch();
        initAboutButton();
        applyWindowInsets();
        initLoginButton();
    }

    /**
     * Enables EdgeToEdge display and sets the content view.
     */
    private void initEdgeToEdgeUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
    }

    /**
     * Initializes the {@link SharedPreferences} instance for storing application preferences.
     */
    private void initSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    /**
     * Initializes the daily study switches (Daf Yomi, Rambam Yomi, Tanah Yomi, Mishna Yomit)
     * and sets their initial states based on saved preferences. It also starts or stops
     * associated background services.
     */
    private void initSwitches() {
        switchDafYomy = findViewById(R.id.switchDafYomy);
        switchRambamYomy = findViewById(R.id.switchRambamYomy);
        switchTanahYomy = findViewById(R.id.switchTanahYomy);
        switchMishnaYomit = findViewById(R.id.switchMishnaYomit);

        initSwitchState(switchDafYomy, "dafYomiSwitchState", DafYomyService.class);
        initSwitchState(switchRambamYomy, "rambamYomiSwitchState", RambamYomyService.class);
        initSwitchState(switchTanahYomy, "tanahYomiSwitchState", TanahYomyService.class);
        initSwitchState(switchMishnaYomit, "mishnaYomitSwitchState", MishnaYomitService.class);
    }

    /**
     * Initializes the daily completion checkboxes (Daf Yomi, Rambam Yomi, Tanah Yomi, Mishna Yomit)
     * and sets their initial states. It also handles resetting them at the start of a new day.
     */
    private void initCheckBoxes() {
        checkBoxDafYomy = findViewById(R.id.checkBoxDafYomy);
        checkBoxRambamYomy = findViewById(R.id.checkBoxRambamYomy);
        checkBoxTanahYomy = findViewById(R.id.checkBoxTanahYomy);
        checkBoxMishnaYomit = findViewById(R.id.checkBoxMishnaYomit);

        initCheckBox(checkBoxDafYomy, "checkbox1_state", "checkbox1_last_update");
        initCheckBox(checkBoxRambamYomy, "checkbox2_state", "checkbox2_last_update");
        initCheckBox(checkBoxTanahYomy, "checkbox3_state", "checkbox3_last_update");
        initCheckBox(checkBoxMishnaYomit, "checkbox4_state", "checkbox4_last_update");
    }

    /**
     * Initializes the click listeners for the notebook buttons, directing to
     * the respective notebook activities.
     */
    private void initNotebookButtons() {
        findViewById(R.id.buttonDafYomy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotebook(DafYomyNotebookActivity.class);
            }
        });
        findViewById(R.id.buttonRambamYomy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotebook(RambamYomyNotebookActivity.class);
            }
        });
        findViewById(R.id.buttonTamahYomy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotebook(TanahYomyNotebookActivity.class);
            }
        });
        findViewById(R.id.buttonMishnaYomit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotebook(MishnaYomitNotebookActivity.class);
            }
        });
    }

    /**
     * Initializes the click listener for the image button that shows the popup menu.
     */
    private void initPopupMenu() {
        findViewById(R.id.imageButtonSupport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });
    }

    /**
     * Handles the logic for the first time the app is launched,
     * specifically requesting notification permissions.
     */
    private void handleFirstLaunch() {
        boolean isFirstLaunch = sharedPreferences.getBoolean("firstLaunch", true);
        if (isFirstLaunch) {
            requestNotificationPermission();
            sharedPreferences.edit().putBoolean("firstLaunch", false).apply();
        }
    }

    /**
     * Initializes the "About App" button and sets its click listener to display the about dialog.
     */
    private void initAboutButton() {
        ImageButton buttonAboutApp = findViewById(R.id.imageButtonInformation);
        buttonAboutApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });
    }

    /**
     * Applies window insets to adjust padding for system bars, ensuring content is not obscured.
     */
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        });
    }

    /**
     * Initializes the login button and sets its click listener to start the {@link LoginActivity}.
     */
    private void initLoginButton() {
        ImageButton imageButtonLogin = findViewById(R.id.imageButtonLogin);
        imageButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Displays an AlertDialog with information about the application.
     */
    private void showAboutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("אודות האפליקציה")
                .setMessage("זוהי אפליקציה ללימוד יומי של דף יומי, רמב״ם יומי, תנ״ך יומי ומשנה יומית.\n\n" +
                        "באפשרותך להפעיל שירותי התראה לכל מסלול לימוד ולשמור הערות שונות לכל לימוד בלחיצה על הכפתור 'הערות'.\n\n" +
                        "מידע חשוב! לחיצה על כפתור 'שחזור גיבוי' תחליף את הטקסט הקיים בטקסט שגובה בעבר (אם לא גובה טקסט, הטקסט הקיים ימחק) ולחיצה על 'גיבוי לענן' תחליף את הגיבוי הקיים בגיבוי חדש. \n\n" +
                        "(גירסה: 0.1)")
                .setPositiveButton("אישור", null)
                .show();
    }

    /**
     * Requests the POST_NOTIFICATIONS permission if the Android version is Tiramisu (API 33) or higher
     * and the permission has not yet been granted.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    /**
     * Callback for the result of requesting permissions.
     *
     * @param requestCode The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either {@link PackageManager#PERMISSION_GRANTED} or {@link PackageManager#PERMISSION_DENIED}. Never null.
     */
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

    /**
     * Initializes a {@link CheckBox} by loading its saved state and setting an
     * {@link CompoundButton.OnCheckedChangeListener} to save its state and the last update time.
     * It also calls {@link #resetCheckBoxIfNeeded(String, String)} to reset the checkbox
     * if a new day has started.
     *
     * @param checkBox The CheckBox to initialize.
     * @param stateKey The key for saving the checked state in {@link SharedPreferences}.
     * @param dateKey The key for saving the last update timestamp in {@link SharedPreferences}.
     */
    private void initCheckBox(CheckBox checkBox, final String stateKey, final String dateKey) {
        resetCheckBoxIfNeeded(stateKey, dateKey);
        boolean isChecked = sharedPreferences.getBoolean(stateKey, false);
        checkBox.setChecked(isChecked);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked1) {
                sharedPreferences.edit()
                        .putBoolean(stateKey, isChecked1)
                        .putLong(dateKey, System.currentTimeMillis())
                        .apply();
            }
        });
    }

    /**
     * Resets a {@link CheckBox}'s state to unchecked if a new day has occurred since its last update.
     *
     * @param stateKey The key for the checkbox's state in {@link SharedPreferences}.
     * @param dateKey The key for the checkbox's last update timestamp in {@link SharedPreferences}.
     */
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

    /**
     * Initializes a {@link Switch} button by loading its saved state and setting an
     * {@link CompoundButton.OnCheckedChangeListener} to save its state.
     * It also starts or stops a specified background service based on the switch's state.
     *
     * @param switchButton The Switch to initialize.
     * @param key The key for saving the switch state in {@link SharedPreferences}.
     * @param serviceClass The Class of the service to start or stop when the switch state changes.
     */
    private void initSwitchState(Switch switchButton, final String key, final Class<?> serviceClass) {
        boolean isSwitchOn = sharedPreferences.getBoolean(key, false);
        switchButton.setChecked(isSwitchOn);
        final Intent serviceIntent = new Intent(this, serviceClass);
        if (isSwitchOn) {
            ContextCompat.startForegroundService(this, serviceIntent);
        }
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean(key, isChecked).apply();
                if (isChecked) {
                    ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
                } else {
                    stopService(serviceIntent);
                }
            }
        });
    }

    /**
     * Opens a new activity representing a notebook.
     *
     * @param notebookClass The Class of the notebook activity to start.
     */
    private void openNotebook(Class<?> notebookClass) {
        startActivity(new Intent(MainActivity.this, notebookClass));
    }

    /**
     * Displays a popup window with options for "Settings" (leading to WhatsApp) and "About" (leading to a phone call).
     *
     * @param view The anchor view for the popup window.
     */
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