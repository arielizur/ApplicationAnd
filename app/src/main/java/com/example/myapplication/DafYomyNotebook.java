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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DafYomyNotebook extends AppCompatActivity {
    private EditText editTextNotebook;
    private SharedPreferences sharedPreferences;
    private static final String NOTE_KEY = "saved_note";
    private Button buttonDelet;
    private Button buttonSaveToCloud;
    private Button buttonLoadFromCloud;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daf_yomy_notebook);

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
                startActivity(new Intent(DafYomyNotebook.this, MainActivity.class));
            }
        });

        buttonSaveNote.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                String noteText = editTextNotebook.getText().toString();
                sharedPreferences.edit().putString(NOTE_KEY, noteText).apply();
                Toast.makeText(DafYomyNotebook.this, "ההערה נשמרה!", Toast.LENGTH_SHORT).show();
            }
        });

        buttonSaveToCloud.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference userRef = database.getReference("users").child(userId).child("daf_yomy");

                    final String noteText = editTextNotebook.getText().toString();
                    userRef.setValue(noteText)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "שמירה נכשלה", e);
                                    Toast.makeText(DafYomyNotebook.this, "שמירה נכשלה!", Toast.LENGTH_SHORT).show();
                                }
                            });

                    // נשמור גם ל-local
                    sharedPreferences.edit().putString(NOTE_KEY, noteText).apply();
                    Toast.makeText(DafYomyNotebook.this, "גיבוי נשמר!", Toast.LENGTH_SHORT).show();

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
                    DatabaseReference userRef = database.getReference("users").child(userId).child("daf_yomy");

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
                Toast.makeText(DafYomyNotebook.this, "ההערה נמחקה!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
