package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button buttonOpenNotebook = findViewById(R.id.button2);
        buttonOpenNotebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DafYomyNotebook.class);
                startActivity(intent);
            }
        });
        Button buttonOpenNotebook1 = findViewById(R.id.button3);
        buttonOpenNotebook1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RambamYomyNoatebook.class);
                startActivity(intent);
            }
        });
        // כפתור תמונה שפותח חלון קופץ
        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(this::showPopupWindow);

        // התאמה לשוליים של המערכת
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showPopupWindow(View view) {
        // יצירת ה-PopupWindow
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_layout, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // חיבור הכפתורים בתוך ה-PopupWindow
        Button buttonSettings = popupView.findViewById(R.id.button_settings);
        Button buttonAbout = popupView.findViewById(R.id.button_about);
        Button buttonClose = popupView.findViewById(R.id.button_close);

        buttonSettings.setOnClickListener(v -> {
            String url = "https://wa.me/972506304230?text=אשמח+לקבל+עזרה";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            popupWindow.dismiss();
        });

        buttonAbout.setOnClickListener(v -> {
            String phoneNumber = "tel:0506304230"; // המספר שברצונך להתקשר אליו
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber));
            startActivity(intent);
            popupWindow.dismiss();
        });

        buttonClose.setOnClickListener(v -> popupWindow.dismiss());

        // הצגת החלון הקופץ ליד כפתור התמונה
        popupWindow.showAsDropDown(view, 0, 0, Gravity.CENTER);


    }
}
