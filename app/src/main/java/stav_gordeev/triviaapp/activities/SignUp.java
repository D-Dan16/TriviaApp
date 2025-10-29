package stav_gordeev.triviaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import stav_gordeev.triviaapp.Helpers.GameGlobalsSingleton;
import stav_gordeev.triviaapp.Helpers.MusicService;
import stav_gordeev.triviaapp.R;
import stav_gordeev.triviaapp.Helpers.User;


/**
 * The SignUp activity manages the user registration process for the application.
 * This screen provides a form for new users to create an account using their email, a password, and a unique username.
 * It performs input validation before using Firebase Authentication to create a new user profile.
 * Upon successful authentication, it also stores the user's information in the Firebase Realtime Database.
 * After a successful registration, the activity navigates the user to the main game screen.
 */
public class SignUp extends BaseActivity {
    // debug TAG
    private static final String TAG = "SignUp";
    // view objects
    TextInputLayout tilEmail, tilPassword, tilUserName;
    TextInputEditText etEmail, etPassword, etUserName;
    TextView tvHiddenRules;
    FloatingActionButton fabRegister, fabCancelReg;
    //String Holders
    String email="", password="", userName="";
    // Firebase Authentication
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // --- Pause Music ---
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("PAUSE");
        startService(playIntent);


        init();

        SignupButtonLogic();


    }  // onCreate

    private void SignupButtonLogic() {
        fabRegister.setOnClickListener(view -> {
            String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(etPassword.getText()).toString();
            String userName = Objects.requireNonNull(etUserName.getText()).toString().trim();

            if (!validateInput(email, password, userName)) {
                return;
            }

            Toast.makeText(SignUp.this, "Signing Up", Toast.LENGTH_SHORT).show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser fbUser = mAuth.getCurrentUser();
                            createUserAndNextActivity(Objects.requireNonNull(fbUser).getUid(), userName);
                        } else {
                            Exception e = task.getException();
                            Log.w(TAG, "createUserWithEmail:failure", e);
                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                tilEmail.setError("Invalid email format.");
                            } else if (e instanceof FirebaseAuthUserCollisionException) {
                                tilEmail.setError("This email is already in use.");
                            } else {
                                Toast.makeText(SignUp.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }

    private boolean validateInput(String email, String password, String userName) {
        boolean isValid = true;

        tilEmail.setError(null);
        tilPassword.setError(null);
        tilUserName.setError(null);

        if (email.isEmpty()) {
            tilEmail.setError("Email is required.");
            isValid = false;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password is required.");
            isValid = false;
        } else if (!isValidPassword(password)) {
            tilPassword.setError("Password must be at least 6 characters long and include a letter and a number.");
            isValid = false;
        }

        if (userName.isEmpty()) {
            tilUserName.setError("Username is required.");
            isValid = false;
        }

        return isValid;
    }


    /**
     * Creates a User object and saves it to the Firebase Realtime Database.
     * On successful creation, it navigates to the Game activity.
     * If the user creation fails, it logs an error and displays a toast message.
     * It also checks if the global question list is ready before starting the game.
     *
     * @param uid The unique user ID from Firebase Authentication.
     * @param userName The username provided by the user during sign-up.
     */
    private void createUserAndNextActivity(String uid, String userName){
        // first create a User object
        User currentUser = new User(uid, userName);
        // Then write it to firebase. first reference the Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // and reference a new node (leaf) in the tree for the new user
        DatabaseReference userNode = database.getReference("Users").child(uid);
        // This is the Firebase Realtime Database write method
        // we place a listener on it, to see that its successful
        // the setValue method returns a Task<Void> - it doesn't return any object
        userNode.setValue(currentUser).addOnCompleteListener(aVoid -> {
            // if successfully
            Log.d(TAG, "User created successfully with uid " + uid);
            Toast.makeText(SignUp.this, "User created successfully. To play, open the menu", Toast.LENGTH_SHORT).show();

            GameGlobalsSingleton.getInstance().setCurrentUser(currentUser);

            Intent toGame = new Intent(this, MainActivity.class);
            startActivity(toGame);
        }).addOnFailureListener(e -> {
            // if failed
            Log.e(TAG, "Failed to create user in database", e);
            Toast.makeText(SignUp.this, "Failed to create user in database.", Toast.LENGTH_SHORT).show();
        });
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
        return password.matches(".*[A-Za-z].*") && password.matches(".*\d.*") && password.length() >= 6;
    }


    private void init(){
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilUserName = findViewById(R.id.tilUserName);
        etEmail = findViewById(R.id.etEmailReg);
        etPassword = findViewById(R.id.etPassword);
        etUserName = findViewById(R.id.etNameReg);
        fabRegister = findViewById(R.id.fabOkReg);
        fabCancelReg = findViewById(R.id.fabCancelReg);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


    }
}
