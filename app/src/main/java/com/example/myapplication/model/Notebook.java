package com.example.myapplication.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Notebook {
    private static final String TAG = "Notebook";

    private String noteText;
    private final SharedPreferences sharedPreferences;
    private final String sharedPrefKey;
    private final String firebasePath;

    public Notebook(Context context, String sharedPrefKey, String firebasePath) {
        this.sharedPrefKey = sharedPrefKey;
        this.firebasePath = firebasePath;
        this.sharedPreferences = context.getSharedPreferences("NotebookPrefs", Context.MODE_PRIVATE);
        this.noteText = sharedPreferences.getString(sharedPrefKey, "");
    }

    public String getNoteText() {
        return noteText;
    }

    /**
     * מעדכן את הטקסט ומשמור מקומית
     */
    public void setNoteText(String noteText) {
        if (noteText == null) noteText = "";
        if (!noteText.equals(this.noteText)) {
            this.noteText = noteText;
            sharedPreferences.edit().putString(sharedPrefKey, noteText).apply();
        }
    }

    /**
     * שומר את ההערה בענן Firebase תחת המשתמש
     */
    public void saveNoteToCloud(FirebaseUser user) {
        if (user == null) {
            Log.w(TAG, "נא להתחבר בשביל לשמור את הגיבוי");
            return;
        }

        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child(firebasePath);

        userRef.setValue(noteText)
                .addOnFailureListener(e -> Log.w(TAG, "שמירה נכשלה", e));
    }

    /**
     * טוען הערה מהענן ושומר מקומית
     * מקבל ראנאבל לפעולה מוצלחת וכישלון
     */
    public void loadNoteFromCloud(FirebaseUser user, Runnable onSuccess, Runnable onFailure) {
        if (user == null) {
            Log.w(TAG, "נא להתחבר בשביל לקבל את הגיבוי");
            if (onFailure != null) onFailure.run();
            return;
        }

        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child(firebasePath);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                noteText = (value != null) ? value : "";
                sharedPreferences.edit().putString(sharedPrefKey, noteText).apply();
                if (onSuccess != null) onSuccess.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "טעינת הערה נכשלה", error.toException());
                if (onFailure != null) onFailure.run();
            }
        });
    }

    /**
     * מוחק את ההערה גם מהזיכרון המקומי
     */
    public void deleteNote() {
        noteText = "";
        sharedPreferences.edit().putString(sharedPrefKey, "").apply();
    }
}
