package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Activity for managing a personal Rambam Yomy notebook.
 * Supports saving notes locally and in Firebase, loading, and deleting.
 */
public class RambamYomyNotebookActivity extends AppCompatActivity {
    private EditText editTextNotebook;
    private SharedPreferences sharedPreferences;
    private static final String NOTE_KEY = "saved_note_1";
    private Button buttonDelete;
    private Button buttonSaveToCloud;
    private Button buttonLoadFromCloud;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rambam_yomy_noatebook);

        initViews();
        loadLocalNote();
        setClickListeners();
    }

    /**
     * Initialize view components and SharedPreferences.
     */
    private void initViews() {
        editTextNotebook = findViewById(R.id.editTextNotebook);
        buttonDelete = findViewById(R.id.buttonDelet);
        buttonSaveToCloud = findViewById(R.id.buttonSaveToCloud);
        buttonLoadFromCloud = findViewById(R.id.buttonLoadFromCloud);
        backButton = findViewById(R.id.backButton);
        sharedPreferences = getSharedPreferences("NotebookPrefs", Context.MODE_PRIVATE);
    }

    /**
     * Load the note from local storage and display it.
     */
    private void loadLocalNote() {
        String savedNote = sharedPreferences.getString(NOTE_KEY, "");
        editTextNotebook.setText(savedNote);
    }

    /**
     * Set click listeners for all buttons.
     */
    private void setClickListeners() {
        findViewById(R.id.buttonSaveNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLocalNote();
            }
        });

        buttonSaveToCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNoteToCloud();
            }
        });

        buttonLoadFromCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNoteFromCloud();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateBack();
            }
        });
    }

    /**
     * Save the current note locally using SharedPreferences.
     */
    private void saveLocalNote() {
        String noteText = editTextNotebook.getText().toString();
        sharedPreferences.edit().putString(NOTE_KEY, noteText).apply();
        Toast.makeText(this, "ההערה נשמרה!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Save the current note to Firebase under the current user's ID.
     * Also saves the note locally.
     */
    private void saveNoteToCloud() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            editTextNotebook.setError("עליך להתחבר כדי לשמור בענן");
            return;
        }

        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("rambam_yomy");

        String noteText = editTextNotebook.getText().toString();

        userRef.setValue(noteText)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "שמירה נכשלה", e);
                        Toast.makeText(RambamYomyNotebookActivity.this, "שמירה נכשלה!", Toast.LENGTH_SHORT).show();
                    }
                });

        sharedPreferences.edit().putString(NOTE_KEY, noteText).apply();
        Toast.makeText(this, "גיבוי נשמר!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Load the note from Firebase and display it.
     * Also saves the loaded note locally.
     */
    private void loadNoteFromCloud() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            editTextNotebook.setError("עליך להתחבר כדי לטעון מהענן");
            return;
        }

        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("rambam_yomy");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                editTextNotebook.setText(value != null ? value : "");
                sharedPreferences.edit().putString(NOTE_KEY, value != null ? value : "").apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
                Toast.makeText(RambamYomyNotebookActivity.this, "טעינה נכשלה!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Delete the note both locally and from the EditText.
     */
    private void deleteNote() {
        editTextNotebook.setText("");
        sharedPreferences.edit().putString(NOTE_KEY, "").apply();
        Toast.makeText(this, "ההערה נמחקה!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Navigate back to MainActivity.
     */
    private void navigateBack() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
