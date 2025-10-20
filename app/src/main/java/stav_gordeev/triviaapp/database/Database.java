package stav_gordeev.triviaapp.database;

import android.app.Activity;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Database {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void saveData(String collection, String document, Map<String, Object> data, OnCompleteListener<Void> onCompleteListener) {
        db.collection(collection).document(document)
                .set(data, SetOptions.merge())
                .addOnCompleteListener(onCompleteListener);
    }

    public static void loadData(String collection, String document, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        db.collection(collection).document(document)
                .get()
                .addOnCompleteListener(onCompleteListener);
    }

    public static void setHighscore(Activity activity, int highscore) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("highscore", highscore);
            saveData("users", Objects.requireNonNull(Authentication.getCurrentUser()).getUid(), data, task -> {
            });
        } catch (NullPointerException e) {
            Toast.makeText(activity, "Error saving highscore", Toast.LENGTH_SHORT).show();
        }
    }

    public static void loadHighscore(Activity activity, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        try {
            loadData("users", Objects.requireNonNull(Authentication.getCurrentUser()).getUid(), onCompleteListener);
        } catch (NullPointerException e) {
            Toast.makeText(activity, "Error loading highscore", Toast.LENGTH_SHORT).show();
        }
    }
}