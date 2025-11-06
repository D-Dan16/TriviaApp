package stav_gordeev.triviaapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import stav_gordeev.triviaapp.Helpers.MusicService;
import stav_gordeev.triviaapp.Helpers.User;
import stav_gordeev.triviaapp.R;


import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class EditUserProperties extends BaseActivity {

    private EditText usernameEditText, emailEditText, etPhoneEditProperty, etPasswordEditProperty;
    private FloatingActionButton saveButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FloatingActionButton bCancelEditProperty;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilUserName;
    private TextInputLayout tilPhoneNumber;

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
        bCancelEditProperty = findViewById(R.id.fabCancelEditProperty);
        usernameEditText = findViewById(R.id.etNameEditProperty);
        etPhoneEditProperty = findViewById(R.id.etPhoneEditProperty);
        etPasswordEditProperty = findViewById(R.id.etPasswordEditProperty);
        emailEditText = findViewById(R.id.etEmailEditProperty);
        saveButton = findViewById(R.id.fabOkEditProperty);
        tilUserName = findViewById(R.id.tilUserName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
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
            String newPassword = etPasswordEditProperty.getText().toString().trim();
            String newPhoneNumber = etPhoneEditProperty.getText().toString().trim();

            FirebaseUser user = mAuth.getCurrentUser();

            if (user == null) {
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validateInput(newEmail, newPassword,newUsername,newPhoneNumber)) {
                return;
            }

            updateFirebaseAuthentication(user, newUsername, newEmail, newPhoneNumber);
        });

        bCancelEditProperty.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
    }

    /**
     * Validates the username and email fields.
     * @return true if input is valid, false otherwise.
     */
    private boolean validateInput(String email, String password, String userName, String phoneNum) {
        boolean isValid = true;

        tilEmail.setError(null);
        tilPassword.setError(null);
        tilUserName.setError(null);
        tilPhoneNumber.setError(null);

        if (email.isEmpty()) {
            tilEmail.setError("Email is required.");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email format.");
            isValid = false;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password is required.");
            isValid = false;
        } else if (!isValidPassword(password)) {
            tilPassword.setError("Password must be at least 6 characters long and include a letter and a number.");
            isValid = false;
        }

        if (phoneNum.length() < 10) {
            tilPhoneNumber.setError("Phone number is too short to be valid.");
            isValid = false;
        }

        if (userName.isEmpty()) {
            tilUserName.setError("Username is required.");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validates the password format.
     * The password must be at least 6 characters long and contain at least one letter and one digit.
     *
     * @param password The password string to validate.
     * @return true if the password is valid, false otherwise.
     */
    private boolean isValidPassword(String password) {
        // Password must be at least 6 characters long and contain at least one letter and one digit
        return password.matches(".*[A-Za-z].*") && password.matches(".*\\d.*") && password.length() >= 6;
    }



    /**
     * Updates the user's profile and email in Firebase Authentication.
     * @param user The current FirebaseUser.
     * @param newUsername The new username.
     */
    private void updateFirebaseAuthentication(FirebaseUser user, String newUsername, String newEmail, String newPhoneNumber) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Now update the email

                user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(emailTask -> {
                    if (emailTask.isSuccessful()) {
                        // Finally, update the Realtime Database
                        updateFirebaseRealtimeDatabase(user.getUid(), newUsername, newPhoneNumber);
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
     *
     * @param userId         The user's unique ID.
     * @param newUsername    The new username.
     */
    private void updateFirebaseRealtimeDatabase(String userId, String newUsername, String newPhoneNumber) {
        DatabaseReference userRef = mDatabase.child("Users").child(userId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("userName", newUsername);
        userData.put("phoneNumber", newPhoneNumber);

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
