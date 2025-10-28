package stav_gordeev.triviaapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import stav_gordeev.triviaapp.Helpers.MusicService;
import stav_gordeev.triviaapp.R;


import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;



public class EditUserProperties extends BaseActivity {

    private EditText usernameEditText, emailEditText;
    private Button saveButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user_properties);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Pause Music ---
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("PAUSE");
        startService(playIntent);


        initializeFirebase();
        initializeViews();
        loadCurrentUser();
        registerButtons();
    }

    /**
     * Initializes Firebase Authentication and Database references.
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Initializes the UI elements from the layout file.
     */
    private void initializeViews() {
        usernameEditText = findViewById(R.id.etNameEditProperty);
        emailEditText = findViewById(R.id.etEmailEditProperty);
        saveButton = findViewById(R.id.fabOkEditProperty);
    }

    /**
     * Loads the current user's data and populates the EditText fields.
     */
    private void loadCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            usernameEditText.setText(currentUser.getDisplayName());
            emailEditText.setText(currentUser.getEmail());
        }
    }

    /**
     * Registers the OnClickListener for the save button.
     */
    private void registerButtons() {
        saveButton.setOnClickListener(v -> {
            String newUsername = usernameEditText.getText().toString().trim();
            String newEmail = emailEditText.getText().toString().trim();
            FirebaseUser user = mAuth.getCurrentUser();

            if (user == null) {
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validateInput(newUsername, newEmail)) {
                return;
            }

            updateFirebaseAuthentication(user, newUsername, newEmail);
        });
    }

    /**
     * Validates the username and email fields.
     * @param username The username to validate.
     * @param email The email to validate.
     * @return true if input is valid, false otherwise.
     */
    private boolean validateInput(String username, String email) {
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username cannot be empty.");
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email cannot be empty.");
            return false;
        }
        return true;
    }

    /**
     * Updates the user's profile and email in Firebase Authentication.
     * @param user The current FirebaseUser.
     * @param newUsername The new username.
     * @param newEmail The new email.
     */
    private void updateFirebaseAuthentication(FirebaseUser user, String newUsername, String newEmail) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Now update the email

                user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(emailTask -> {
                    if (emailTask.isSuccessful()) {
                        // Finally, update the Realtime Database
                        updateFirebaseRealtimeDatabase(user.getUid(), newUsername, newEmail);
                    } else {
                        Toast.makeText(EditUserProperties.this, "Failed to update email: " + emailTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(EditUserProperties.this, "Failed to update profile: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Updates the user's data in the Firebase Realtime Database.
     * @param userId The user's unique ID.
     * @param newUsername The new username.
     * @param newEmail The new email.
     */
    private void updateFirebaseRealtimeDatabase(String userId, String newUsername, String newEmail) {
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", newUsername);
        userData.put("email", newEmail);

        userRef.updateChildren(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditUserProperties.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity after saving
            } else {
                Toast.makeText(EditUserProperties.this, "Failed to update database: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
