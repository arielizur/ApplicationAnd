package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DafYomyNotebook extends AppCompatActivity {
    private EditText editTextNotebook;
    private SharedPreferences sharedPreferences;
    private static final String NOTE_KEY = "saved_note";
    private Button buttonDelete, buttonSaveToCloud, buttonDeleteCloud;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daf_yomy_notebook);

        editTextNotebook = findViewById(R.id.editTextNotebook);
        Button buttonSaveNote = findViewById(R.id.buttonSaveNote);
        buttonDelete = findViewById(R.id.buttonDelet);
        buttonSaveToCloud = findViewById(R.id.buttonSaveToCloud);
        buttonDeleteCloud = findViewById(R.id.buttonSaveToCloud); // כפתור חדש למחיקת הערה מהענן

        sharedPreferences = getSharedPreferences("NotebookPrefs", Context.MODE_PRIVATE);
        editTextNotebook.setText(sharedPreferences.getString(NOTE_KEY, ""));

        // אתחול Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("notes");

        // אתחול ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("שומר הערה...");

        buttonSaveNote.setOnClickListener(v -> saveNote());
        buttonDelete.setOnClickListener(v -> deleteNote());
        buttonSaveToCloud.setOnClickListener(v -> saveNoteToCloud());
        buttonDeleteCloud.setOnClickListener(v -> deleteNoteFromCloud());
    }

    private void saveNote() {
        String noteText = editTextNotebook.getText().toString().trim();
        sharedPreferences.edit().putString(NOTE_KEY, noteText).apply();
        Toast.makeText(this, "ההערה נשמרה!", Toast.LENGTH_SHORT).show();
    }

    private void deleteNote() {
        editTextNotebook.setText("");
        sharedPreferences.edit().remove(NOTE_KEY).apply();
        Toast.makeText(this, "ההערה נמחקה!", Toast.LENGTH_SHORT).show();
    }

    private void saveNoteToCloud() {
        String noteText = editTextNotebook.getText().toString().trim();
        if (noteText.isEmpty()) {
            Toast.makeText(this, "לא ניתן לשמור הערה ריקה", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show(); // הצגת טוען

        String noteId = databaseReference.push().getKey();
        if (noteId == null) {
            Toast.makeText(this, "שגיאה ביצירת מזהה להערה", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child(noteId).setValue(noteText)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(DafYomyNotebook.this, "ההערה נשמרה בענן!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(DafYomyNotebook.this, "שגיאה בשמירת ההערה בענן", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteNoteFromCloud() {
        databaseReference.removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(DafYomyNotebook.this, "כל ההערות נמחקו מהענן!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(DafYomyNotebook.this, "שגיאה במחיקת ההערות מהענן", Toast.LENGTH_SHORT).show());
    }
}
