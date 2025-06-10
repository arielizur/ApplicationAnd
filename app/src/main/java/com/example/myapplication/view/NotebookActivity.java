package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Notebook;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * NotebookActivity manages the user interface for a single notebook.
 * It allows users to write, save, delete, and sync their notes with the cloud.
 */
public class NotebookActivity extends AppCompatActivity {

    /**
     * Tag for logging purposes.
     */
    private static final String TAG = "NotebookActivity";

    /**
     * The EditText view where the user writes their note.
     */
    private EditText editTextNotebook;

    /**
     * Buttons for various actions like saving locally, deleting, and cloud operations.
     */
    private Button buttonSaveNote, buttonDelete, buttonSaveToCloud, buttonLoadFromCloud, backButton;

    /**
     * The business logic and data handling for the notebook.
     */
    private Notebook notebook;

    /**
     * Initializes the activity, sets up the views, and loads the notebook data.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        if (!initializeNotebookData()) {
            return;
        }
        initViews();
        editTextNotebook.setText(notebook.getNoteText());
        setupButtonListeners();
    }

    /**
     * Initializes the Notebook object based on intent extras.
     * @return true if initialization was successful, false otherwise.
     */
    private boolean initializeNotebookData() {
        Intent intent = getIntent();
        String localKey = intent.getStringExtra("localKey");
        String firebasePath = intent.getStringExtra("firebasePath");

        if (localKey == null || firebasePath == null) {
            Toast.makeText(this, "Missing notebook info", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        notebook = new Notebook(this, localKey, firebasePath);
        return true;
    }

    /**
     * Initializes all the UI components from the layout file.
     */
    private void initViews() {
        editTextNotebook = findViewById(R.id.editTextNotebook);
        buttonSaveNote = findViewById(R.id.buttonSaveNote);
        buttonDelete = findViewById(R.id.buttonDelet); // Typo in original ID "buttonDelet" is preserved
        buttonSaveToCloud = findViewById(R.id.buttonSaveToCloud);
        buttonLoadFromCloud = findViewById(R.id.buttonLoadFromCloud);
        backButton = findViewById(R.id.backButton);
    }

    /**
     * Sets up the OnClickListener for all the buttons in the activity.
     * This method is now responsible for defining all button behaviors.
     */
    private void setupButtonListeners() {
        buttonSaveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNoteLocally();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNoteLocally();
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

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Handles saving the current note text to local storage.
     */
    private void saveNoteLocally() {
        String text = editTextNotebook.getText().toString();
        notebook.setNoteText(text);
        Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles deleting the note from local storage.
     */
    private void deleteNoteLocally() {
        notebook.deleteNote();
        editTextNotebook.setText("");
        Toast.makeText(this, "Note deleted!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles saving the current note text to Firebase Cloud.
     * Requires user to be logged in.
     */
    private void saveNoteToCloud() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            editTextNotebook.setError("You must be logged in to save to the cloud");
            return;
        }
        String text = editTextNotebook.getText().toString();
        notebook.setNoteText(text);
        notebook.saveNoteToCloud(user);
        Toast.makeText(this, "Backup saved!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles loading the note text from Firebase Cloud.
     * Requires user to be logged in.
     */
    private void loadNoteFromCloud() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            editTextNotebook.setError("You must be logged in to load from the cloud");
            return;
        }
        notebook.loadNoteFromCloud(user,
                // Success callback
                new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editTextNotebook.setText(notebook.getNoteText());
                                Toast.makeText(NotebookActivity.this, "Load finished", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                },
                // Failure callback
                new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(NotebookActivity.this, "Load failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }
}