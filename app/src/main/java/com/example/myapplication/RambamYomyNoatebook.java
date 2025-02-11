package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class RambamYomyNoatebook extends AppCompatActivity {
    private EditText editTextNotebook;
    private SharedPreferences sharedPreferences;
    private static final String NOTE_KEY = "saved_note_1";
    private Button buttonDelet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rambam_yomy_noatebook);

        editTextNotebook = findViewById(R.id.editTextNotebook);
        Button buttonSaveNote = findViewById(R.id.buttonSaveNote):
        buttonDelet = findViewById(R.id.buttonDelet);

        // טעינת ההערה השמורה
        sharedPreferences = getSharedPreferences("NotebookPrefs", Context.MODE_PRIVATE);
        editTextNotebook.setText(sharedPreferences.getString(NOTE_KEY, ""));

        buttonSaveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noteText = editTextNotebook.getText().toString();
                sharedPreferences.edit().putString(NOTE_KEY, noteText).apply();
            }
        });
        buttonDelet.setOnClickListener(v -> {
            editTextNotebook.setText("");
            sharedPreferences.edit().putString(NOTE_KEY, "").apply();
        });
    }
}
