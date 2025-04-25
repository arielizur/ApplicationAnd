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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TanahYomyNoatebook extends AppCompatActivity {
        private EditText editTextNotebook;
        private SharedPreferences sharedPreferences;
        private static final String NOTE_KEY = "saved_note_2";
        private Button buttonDelet;
        private Button buttonSaveToCloud;
        private Button buttonLoadFromCloud;
        private Button backButton;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.tanah_yomy_noatebook);

            editTextNotebook = findViewById(R.id.editTextNotebook);
            Button buttonSaveNote = findViewById(R.id.buttonSaveNote);
            buttonDelet = findViewById(R.id.buttonDelet);
            buttonSaveToCloud = findViewById(R.id.buttonSaveToCloud);
            buttonLoadFromCloud = findViewById(R.id.buttonLoadFromCloud);
            backButton = findViewById(R.id.backButton);

            sharedPreferences = getSharedPreferences("NotebookPrefs", Context.MODE_PRIVATE);
            editTextNotebook.setText(sharedPreferences.getString(NOTE_KEY, ""));

            backButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    startActivity(new Intent(TanahYomyNoatebook.this, MainActivity.class));
                }
            });

            buttonSaveNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String noteText = editTextNotebook.getText().toString();
                    sharedPreferences.edit().putString(NOTE_KEY, noteText).apply();
                    Toast.makeText(TanahYomyNoatebook.this, "ההערה נשמרה!", Toast.LENGTH_SHORT).show();
                }
            });

            buttonSaveToCloud.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String userId = user.getUid();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference userRef = database.getReference("users").child(userId).child("tanah_yomy");

                        String noteText = editTextNotebook.getText().toString();
                        userRef.setValue(noteText);
                        Toast.makeText(TanahYomyNoatebook.this, "גיבוי נשמר!", Toast.LENGTH_SHORT).show();
                    } else {
                        editTextNotebook.setError("עליך להתחבר כדי לשמור בענן");
                    }
                }
            });

            buttonLoadFromCloud.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String userId = user.getUid();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference userRef = database.getReference("users").child(userId).child("tanah_yomy");

                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String value = dataSnapshot.getValue(String.class);
                                if (value != null) {
                                    editTextNotebook.setText(value);
                                    sharedPreferences.edit().putString(NOTE_KEY, value).apply();
                                } else {
                                    editTextNotebook.setText("");
                                }
                                String noteText = editTextNotebook.getText().toString();
                                sharedPreferences.edit().putString(NOTE_KEY, noteText).apply();
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Log.w(TAG, "Failed to read value.", error.toException());
                            }
                        });
                    } else {
                        editTextNotebook.setError("עליך להתחבר כדי לטעון מהענן");
                    }
                }
            });

            buttonDelet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTextNotebook.setText("");
                    sharedPreferences.edit().putString(NOTE_KEY, "").apply();
                    Toast.makeText(TanahYomyNoatebook.this, "ההערה נמחקה!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }