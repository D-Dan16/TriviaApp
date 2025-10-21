package stav_gordeev.triviaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
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

import stav_gordeev.triviaapp.R;
import stav_gordeev.triviaapp.User;

public class MainActivity extends BaseActivity {
    private FloatingActionButton fabRegister;
    private FloatingActionButton fabLogin;
    private CheckBox cbPersonal;
    private EditText etEmail;
    private EditText etPassword;

    String TAG = "Login";
    // UI elements
    TextInputLayout tilEmailIn, tilPasswordIn ;
    TextInputEditText etEmailIn,  etPasswordIn ;
    FloatingActionButton bSignIn;

    Context context;
    TextView tvSignUpHere;
    TextView tvForgotPassword;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

        mAuth = FirebaseAuth.getInstance();
        usersDBRef = FirebaseDatabase.getInstance().getReference("Users");

        setClickableLinks();

        // Listener for the Sign In Button
        bSignIn.setOnClickListener(v -> {
            email = Objects.requireNonNull(etEmailIn.getText()).toString();
            password = Objects.requireNonNull(etPasswordIn.getText()).toString();
            if (email.isEmpty()) {
                tilEmailIn.setError("Email Required");
                return;
            }
            if (password.isEmpty()) {
                tilPasswordIn.setError("Password Required");
                return;
            }
            Toast.makeText(MainActivity.this, "Signing In", Toast.LENGTH_SHORT).show();

            // this is firebase method to sign in user with email and password
            // it is asynchronic so we need a callback
            // we set a listener on it with a task object
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
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
                                        // Starts next activity (= fetch questions)
                                        Intent goToGame = new Intent(context, Game.class);
                                        startActivity(goToGame);
                                    }
                                    else {
                                        Log.e(TAG, "User objects returned NULL");
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
                        else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            // check why it failed (wrong password? email not registered?)
                            Exception exception = task.getException();
                            String errorMessage = "Authentication failed."; // Default message

                            if (exception instanceof FirebaseAuthInvalidUserException) {
                                // No user record found for the provided email address.
                                // This could mean the email is not registered.
                                errorMessage = "No account found with this email address.";
                                Log.e(TAG, "FirebaseAuthInvalidUserException: " + exception.getMessage());
                                // You might want to update a specific UI element, e.g., tilEmail.setError(errorMessage);
                            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                // The supplied credential (usually password) is wrong.
                                errorMessage = "Incorrect password. Please try again.";
                                Log.e(TAG, "FirebaseAuthInvalidCredentialsException: " + exception.getMessage());
                                // You might want to update a specific UI element, e.g., tilPassword.setError(errorMessage);
                            } else if (exception instanceof FirebaseNetworkException) {
                                // A network error (such as timeout, interrupted connection or unreachable host) has occurred.
                                errorMessage = "Network error. Please check your connection.";
                                Log.e(TAG, "FirebaseNetworkException: " + exception.getMessage());
                            } else {
                                // Other types of exceptions (less common for typical login failures)
                                // You can get the generic message from the exception
                                if (exception != null && exception.getMessage() != null) {
                                    errorMessage = exception.getMessage();
                                }
                                Log.e(TAG, "Other authentication error: " + errorMessage);
                            }

                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                        }
                    });


        });


    }


    private void setClickableLinks() {
        // listener for the sign up clickable String
        ClickableSpan clickableSignUp = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Intent to navigate to your SignUpActivity
                Intent go2SignUp = new Intent(context, SignUp.class);
                startActivity(go2SignUp);
            }
        };
        // Find the start and end of the clickable text
        int startIndex = fullTextSignUp.indexOf(clickableTextSignUp);
        int endIndex = startIndex + clickableTextSignUp.length();

        if (startIndex != -1) {
            // Apply the clickable span
            spannableSignUp.setSpan(clickableSignUp, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Apply an underline span
            spannableSignUp.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        // Sets the Link in the TextView and Makes the link clickable
        tvSignUpHere.setText(spannableSignUp);
        tvSignUpHere.setMovementMethod(LinkMovementMethod.getInstance());

        // listener for the Forgot Password clickable String
        ClickableSpan clickableForgotPassword = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                email = Objects.requireNonNull(etEmailIn.getText()).toString();
                if (email.isEmpty()) {
                    tilEmailIn.setError("Email Required");
                } else {
                    // Firebase Method to send reset password email
                    // it is asynchronic so we need a callback on it
                    // we place a listener on it with a task object
                    // its either successful or else
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Email sent successfully
                                    Log.d(TAG, "Email sent.");
                                    Toast.makeText(context, "Password reset Email sent.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(TAG, "Failed to send password reset email", task.getException());
                                    Toast.makeText(context, "Failed to send password reset email.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        };

        startIndex = fullTextForgotPassword.indexOf(clickableTextForgotPassword);
        endIndex = startIndex + clickableTextForgotPassword.length();

        if (startIndex != -1) {
            spannableForgotPassword.setSpan(clickableForgotPassword, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableForgotPassword.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvForgotPassword.setText(spannableForgotPassword);
        tvForgotPassword.setMovementMethod(LinkMovementMethod.getInstance());
    }

    void initUI() {
        context = this;
        tilEmailIn = findViewById(R.id.tilEmail);
        tilPasswordIn = findViewById(R.id.tilPassword);
        etEmailIn = findViewById(R.id.etEmailReg);
        etPasswordIn = findViewById(R.id.etPassword);
        bSignIn = findViewById(R.id.fabOkReg);
        spannableSignUp = new SpannableString(fullTextSignUp);
        spannableForgotPassword = new SpannableString(fullTextForgotPassword);
    }
}
