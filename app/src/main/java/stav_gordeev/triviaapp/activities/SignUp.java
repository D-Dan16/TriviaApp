package stav_gordeev.triviaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


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

import stav_gordeev.triviaapp.R;
import stav_gordeev.triviaapp.User;


public class SignUp extends AppCompatActivity {


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


        init();


        // when button pushed
        fabRegister.setOnClickListener(view -> {
            String email = Objects.requireNonNull(etEmail.getText()).toString();
            String password = Objects.requireNonNull(etPassword.getText()).toString();
            String userName = Objects.requireNonNull(etUserName.getText()).toString();


            if(email.isEmpty()){
                tilEmail.setError("Email Required");
                return;
            }
            if(password.isEmpty()){
                tilPassword.setError("Password Required");
                return;
            }
            if(userName.isEmpty()){
                tilUserName.setError("UserName Required");
                return;
            }
            if(!isValidPassword(password)){
                tilPassword.setError("Incorrect Password Format");
                return;
            }


            Toast.makeText(SignUp.this, "Signing Up", Toast.LENGTH_SHORT).show();


            // method from the firebase authentication library to create a new user with email / password
            // it is asynchronic, so we need a callback
            // we place a listener on it with a task object.
            // we implement actions if the task is successful and if not
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser fbUser = mAuth.getCurrentUser();
                            createUserAndNextActivity(Objects.requireNonNull(fbUser).getUid(), userName);
                        }
                        else {
                            // check why it failed
                            Exception e = task.getException();
                            Log.w(TAG, "createUserWithEmail:failure", e);
                            String errorMessage = Objects.requireNonNull(e).getMessage();
                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                // invalid credentials. which credential?
                                Log.e(TAG, "Invalid Credentials: "+errorMessage);
                                if(Objects.requireNonNull(errorMessage).contains("ERROR_INVALID_EMAIL"))
                                    tilEmail.setError("Valid email address Please!");
                            }
                            else if (e instanceof FirebaseAuthUserCollisionException) {
                                // user already exists
                                Log.e(TAG, "User already exists: "+errorMessage);
                                tilEmail.setError("This Email is already used!");
                            }
                            else {
                                Log.e(TAG, "Unknown error: " + errorMessage);
                                Toast.makeText(SignUp.this, "Unknown error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }


                        }
                    }); // listener on the task


        }); // listener on the button


    }  // onCreate


    private void createUserAndNextActivity(String uid, String userName){
        // first create a User object
        User currentUser = new User(uid, userName);
        // Then write it to firebase. first reference the Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // and reference a new node (leaf) in the tree for the new user
        DatabaseReference userNode = database.getReference("Users").child(uid);
        // This is the Firebase Realtime Database write method
        // we place a listener on it, to see that its successful
        // the setValue method returns a Task<Void> - it doesnt return any object
        userNode.setValue(currentUser).addOnCompleteListener(aVoid -> {
            // if successfull
            Log.d(TAG, "User created successfully with uid " + uid);
            Toast.makeText(SignUp.this, "User created successfully.", Toast.LENGTH_SHORT).show();
            // go to dbFetchWait screen
            Intent toGame = new Intent(this, Game.class);
            startActivity(toGame);
        }).addOnFailureListener(e -> {
            // if failed
            Log.e(TAG, "Failed to create user in database", e);
            Toast.makeText(SignUp.this, "Failed to create user in database.", Toast.LENGTH_SHORT).show();
        });
    }


    private boolean isValidPassword(String password) {
        // Password must be at least 6 characters long and contain at least one letter and one digit
        return password.matches(".*[A-Za-z].*") && password.matches(".*\\d.*") && password.length() >= 6;
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