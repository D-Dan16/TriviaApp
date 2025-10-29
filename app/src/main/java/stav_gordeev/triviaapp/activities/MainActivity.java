package stav_gordeev.triviaapp.activities;

import static stav_gordeev.triviaapp.Helpers.TriviaQuestionGenerator.createQuestionListInBackground;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import android.content.Context;

import java.util.Objects;

import stav_gordeev.triviaapp.Helpers.MusicService;
import stav_gordeev.triviaapp.R;
import stav_gordeev.triviaapp.Helpers.User;

/**
 * Represents the main login screen of the application.
 * This activity handles user authentication (sign-in) via Firebase Authentication.
 * It provides options to sign in, navigate to the sign-up screen, and reset a forgotten password.
 * The activity also manages user preferences for remembering login credentials using SharedPreferences.
 * Upon successful login, it retrieves the user's data from Firebase Realtime Database and navigates
 * to the main game screen. It also pre-fetches trivia questions in the background to ensure a smooth
 * transition to the game.
 *
 * @author Stav Gordeev
 * @see SignUp
 * @see Game
 * @see BaseActivity
 */
public class MainActivity extends BaseActivity {
    //region Fields
    String TAG = "Login";
    // UI elements
    TextInputLayout tilEmailIn, tilPasswordIn ;
    TextInputEditText etEmailReg,  etPassword;
    FloatingActionButton bSignIn, bSignUp;

    Context context;
    // String Holders
    String email="", password="";
    // Spannable String Holders
    String fullTextSignUp = "Not signed up yet? Sign up here";
    String clickableTextSignUp = "Sign up here";
    String fullTextForgotPassword = "Forgot Password?\nEnter your Email and Click here";
    String clickableTextForgotPassword = "Click here";
    SpannableString spannableSignUp, spannableForgotPassword;
    private FirebaseAuth mAuth;     // Firebase Authentication Object
    DatabaseReference usersDBRef;   // Firebase Realtime Database Users Reference
    String uid="";                 // Firebase User ID to be retrieved from Firebase Authentication
    User currentUser;// User object to be constructed

    SharedPreferences sharedPref;
    private CheckBox cbPersonal;
    private Button bDeletePreferences;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Pause Music ---
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("STOP");
        startService(playIntent);


        sharedPref = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);

        initUI();

        mAuth = FirebaseAuth.getInstance();
        usersDBRef = FirebaseDatabase.getInstance().getReference("Users");

        addUserPreferencesDataToEditTexts();

        createQuestionListInBackground(context);

        // Listener for the Sign In Button
        signInButtonLogic();

        signUpButtonLogic();

        deleteUserPreferencesButton();

    }

    /**
     * Clears saved SharedPreferences when invoked by delete button.
     */
    private void deleteUserPreferencesButton() {
        bDeletePreferences.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();
        });
    }


    private void signUpButtonLogic() {
        bSignUp.setOnClickListener(v -> {
            Intent goToSignUp = new Intent(context, SignUp.class);
            startActivity(goToSignUp);
        });
    }


    /**
     * Sets up the logic for the sign-in button by attaching an OnClickListener.
     * It validates the user's email and password, checks if trivia questions are loaded, and then attempts to authenticate with Firebase.
     * Upon successful authentication, it proceeds to the main game activity; otherwise, it logs the failure reason and displays an error message.
     */
    private void signInButtonLogic() {
        bSignIn.setOnClickListener( v -> {
            email = Objects.requireNonNull(etEmailReg.getText()).toString();
            password = Objects.requireNonNull(etPassword.getText()).toString();
            if (email.isEmpty()) {
                tilEmailIn.setError("Email Required");
                return;
            }
            if (password.isEmpty()) {
                tilPasswordIn.setError("Password Required");
                return;
            }

            if (GameGlobalsSingleton.getInstance().hasQuestions) {
                Toast.makeText(MainActivity.this, "Wait a little more until the questions are ready to be presented ", Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(MainActivity.this, "Signing In", Toast.LENGTH_SHORT).show();

            // this is firebase method to sign in user with email and password
            // it is asynchronic so we need a callback
            // we set a listener on it with a task object
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            executeSignIn(task);
                        } else {
                            logReasonForUnsuccessfulSignIn(task);
                        }
                    });


        });
    }

    /**
     * Logs the specific reason for a failed sign-in attempt and displays a user-friendly
     * error message.
     * <p>
     * This method is called when a Firebase `signInWithEmailAndPassword` task fails. It inspects
     * the exception from the task to determine the cause of the failure (e.g., invalid user,
     * wrong password, network issues) and logs a detailed error. It then presents a
     * corresponding, easy-to-understand message to the user via a Toast.
     *
     * @param task The completed, unsuccessful Task from the Firebase sign-in attempt.
     */
    private void logReasonForUnsuccessfulSignIn(Task<AuthResult> task) {
        // If sign in fails, display a message to the user.
        Log.w(TAG, "signInWithEmail:failure", task.getException());
        // check why it failed (wrong password? email not registered?)
        Exception exception = task.getException();
        String errorMessage = "Authentication failed."; // Default message

        if (exception instanceof FirebaseAuthInvalidUserException) {
            // No user record found for the provided email address.
            // This could mean the email is not registered.
            errorMessage = "No account found with this email address.";
            Log.e(TAG, "FirebaseAuthInvalidUserException: " + exception.getMessage(), task.getException());
            // You might want to update a specific UI element, e.g., tilEmail.setError(errorMessage);
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            // The supplied credential (usually password) is wrong.
            errorMessage = "Incorrect password. Please try again.";
            Log.e(TAG, "FirebaseAuthInvalidCredentialsException: " + exception.getMessage(), task.getException());
            // You might want to update a specific UI element, e.g., tilPassword.setError(errorMessage);
        } else if (exception instanceof FirebaseNetworkException) {
            // A network error (such as timeout, interrupted connection or unreachable host) has occurred.
            errorMessage = "Network error. Please check your connection.";
            Log.e(TAG, "FirebaseNetworkException: " + exception.getMessage(), task.getException());
        } else {
            // Other types of exceptions (less common for typical login failures)
            // You can get the generic message from the exception
            if (exception != null && exception.getMessage() != null) {
                errorMessage = exception.getMessage();
            }
            Log.e(TAG, "Other authentication error: " + errorMessage, task.getException());
        }

        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Finalizes the sign-in process after a successful Firebase authentication.
     * This method saves user credentials if "remember me" is checked, then retrieves the user's unique ID.
     * It then fetches the corresponding user object from the Firebase Realtime Database and stores it in a global singleton.
     * Finally, it navigates the user to the main game activity.
     */
    private void executeSignIn(Task<AuthResult> task) {
        // Sign in success, update UI with the signed-in user's information
        if (cbPersonal.isChecked()) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("email", email);
            editor.putString("password", password);
            editor.apply();
        }


        // Sign in success, log it, and get the specific uid
        Log.d(TAG, "signInWithEmail:success");
        assert mAuth.getCurrentUser() != null;
        uid=mAuth.getCurrentUser().getUid();

        // Now we want to retrieve the User Object from the Realtime Database
        // and assign it in the GameGlobals Singleton
        // to be accessed through the game
        Toast.makeText(MainActivity.this, "Retrieving User Information", Toast.LENGTH_SHORT).show();
        // this is ASynchronic, so we need a callback
        // we put a single time listener on it
        usersDBRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override   // this is a callback method - when i get data from the database
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // gets value from the database, and set it in the User object
                currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) {  // double check that it worked
                    Log.d(TAG, "User retrieved successfully with uid " + uid);
                    // sets the User object in the GameGlobals available to all Activities
                    GameGlobalsSingleton.getInstance().setCurrentUser(currentUser);


                    Intent goToGame = new Intent(context, Game.class);
                    startActivity(goToGame);
                }
                else {
                    Log.e(TAG, "User objects returned NULL", task.getException());
                    Toast.makeText(MainActivity.this, "Failed to retrieve User information", Toast.LENGTH_SHORT).show();
                }
            }
            @Override   // this is another callback method - did not find the user object
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to retrieve User object", databaseError.toException());
                Toast.makeText(MainActivity.this, "Failed to retrieve User information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Retrieves user's email and password from SharedPreferences, if they exist,
     * and pre-fills the corresponding EditText fields. This is typically used to
     * remember user credentials if they previously opted to save them. The method
     * gracefully handles cases where the preferences might not exist by catching
     * exceptions.
     */
    private void addUserPreferencesDataToEditTexts() {
        try {
            String password = sharedPref.getString("password", "");
            etPassword.setText(password);
        } catch (Exception ignored) {}

        try {
            String email = sharedPref.getString("email", "");
            etEmailReg.setText(email);
        } catch (Exception ignored) {}
    }
    void initUI() {
        context = this;
        tilEmailIn = findViewById(R.id.tilEmail);
        tilPasswordIn = findViewById(R.id.tilPassword);
        etEmailReg = findViewById(R.id.etEmailReg);
        etPassword = findViewById(R.id.etPassword);
        cbPersonal = findViewById(R.id.cbPersonal);
        bSignIn = findViewById(R.id.fabOkReg);
        bSignUp = findViewById(R.id.fabRegister);
        spannableSignUp = new SpannableString(fullTextSignUp);
        spannableForgotPassword = new SpannableString(fullTextForgotPassword);
        bDeletePreferences = findViewById(R.id.bDeletePreferences);

        tilEmailIn.setHint("Email");
        tilPasswordIn.setHint("Password");
    }
}
