package stav_gordeev.triviaapp.database;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;

import stav_gordeev.triviaapp.activities.Game;
import stav_gordeev.triviaapp.activities.Register;

public class Authentication {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static void signIn(Activity activity, String email, String password, OnCompleteListener<AuthResult> onCompleteListener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity, onCompleteListener);
    }

    public static void signOut() {
        auth.signOut();
    }

    public static void signUp(Activity activity, String email, String password, OnCompleteListener<AuthResult> onCompleteListener) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, onCompleteListener);
    }


    public static FirebaseUser getCurrentUser() {
        if (auth.getCurrentUser() == null) return null;

        return auth.getCurrentUser();
    }
}