package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * LoginActivity handles user authentication using Firebase.
 * It allows users to log in or register and navigate to the main activity upon success.
 */
public class LoginActivity extends AppCompatActivity {

    /** Firebase Authentication instance */
    private FirebaseAuth mAuth;

    /** Input field for user email */
    private EditText emailEditText;

    /** Input field for user password */
    private EditText passwordEditText;

    /** Button for login action */
    private Button loginButton;

    /** Button for registration action */
    private Button registerButton;

    /** Button to return to the main screen */
    private Button backButton;

    /**
     * Initializes the activity, views, Firebase, and edge-to-edge UI.
     *
     * @param savedInstanceState the saved state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        setupEdgeToEdge();
        initFirebase();
        initViews();
        setupListeners();
    }

    /**
     * Enables edge-to-edge display by applying system insets padding to the root view.
     */
    private void setupEdgeToEdge() {
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, new androidx.core.view.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
            });
        }
    }

    /**
     * Initializes Firebase Authentication.
     */
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Finds and assigns views from the layout.
     */
    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);
    }

    /**
     * Sets up listeners for login, register, and back buttons.
     */
    private void setupListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToMain();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });
    }

    /**
     * Navigates the user to the main activity.
     */
    private void navigateToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    /**
     * Validates user input and attempts to log in with Firebase.
     */
    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateFields(email, password)) return;

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        handleLoginResult(task);
                    }
                });
    }

    /**
     * Validates user input and attempts to register with Firebase.
     */
    private void attemptRegister() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateFields(email, password)) return;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        handleRegisterResult(task);
                    }
                });
    }

    /**
     * Validates that both email and password fields are filled.
     *
     * @param email The user's email input
     * @param password The user's password input
     * @return true if both fields are non-empty; false otherwise
     */
    private boolean validateFields(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Handles the result of the login attempt.
     *
     * @param task The task representing the login result
     */
    private void handleLoginResult(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()) {
            Toast.makeText(this, "התחברת בהצלחה", Toast.LENGTH_SHORT).show();
            navigateToMain();
            finish();
        } else {
            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handles the result of the registration attempt.
     *
     * @param task The task representing the registration result
     */
    private void handleRegisterResult(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()) {
            Toast.makeText(this, "נרשמת והתחברת בהצלחה!", Toast.LENGTH_SHORT).show();
            navigateToMain();
            finish();
        } else {
            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
