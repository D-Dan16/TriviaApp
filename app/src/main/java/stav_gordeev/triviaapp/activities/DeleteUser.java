package stav_gordeev.triviaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import stav_gordeev.triviaapp.Helpers.GameGlobalsSingleton;
import stav_gordeev.triviaapp.Helpers.User;
import stav_gordeev.triviaapp.R;

/**
 * An activity that provides the user with an option to delete their own account.
 * It handles the logic for finding the current user in the Firebase Realtime Database and removing their data.
 */
public class DeleteUser extends BaseActivity {
    Button bDeleteUser,bGoBackToMainActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delete_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        registerButtons();
    }


    /**
     * Registers and sets up the onClick listeners for the buttons in the activity.
     * This includes the 'yes' button for confirming user deletion and the 'no' button
     * for canceling the action and returning to the previous screen.
     */
    private void registerButtons() {
        bGoBackToMainActivity.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        bDeleteUser.setOnClickListener(v -> {
            var usersDBRef = FirebaseDatabase.getInstance().getReference("Users");
            User curUser = GameGlobalsSingleton.getInstance().getCurrentUser();

            // Fetch all users to find the one to delete
            Task<DataSnapshot> dataSnapshotTask = usersDBRef.get();
            dataSnapshotTask.addOnSuccessListener(dataSnapshot -> {
                for (var child : dataSnapshot.getChildren()) {
                    var user = child.getValue(User.class);
                    if (user == null) continue;

                    // If a user with a matching username is found, remove them
                    if (Objects.equals(user.getUserName(), curUser.getUserName())) {
                        usersDBRef.child(child.getKey()).removeValue();
                        GameGlobalsSingleton.getInstance().setCurrentUser(null);
                        return; // Exit the loop once the user is found and removed
                    }
                }

                // In case the user was not found in the DB, still log them out locally
                GameGlobalsSingleton.getInstance().setCurrentUser(null);

            });

            // Navigate back to the main screen after initiating the deletion
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        bDeleteUser = findViewById(R.id.bDeleteUser);
        bGoBackToMainActivity = findViewById(R.id.bGoBackToMainActivity);

    }
}
