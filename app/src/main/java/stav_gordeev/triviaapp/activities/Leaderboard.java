package stav_gordeev.triviaapp.activities;

import static java.lang.String.valueOf;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import kotlin.jvm.internal.markers.KMutableList;
import stav_gordeev.triviaapp.Helpers.MusicService;
import stav_gordeev.triviaapp.Helpers.User;
import stav_gordeev.triviaapp.R;

public class Leaderboard extends AppCompatActivity {
    TextView tvRankName1, tvRankName2, tvRankName3, tvRankName4, tvRankName5;

    TextView tvScore1, tvScore2, tvScore3, tvScore4, tvScore5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Pause Music ---
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("STOP");
        startService(playIntent);

        initViews();
        displayLeaderboard();
    }

    private void initViews() {
        tvRankName1 = findViewById(R.id.tvRankName1);
        tvRankName2 = findViewById(R.id.tvRankName2);
        tvRankName3 = findViewById(R.id.tvRankName3);
        tvRankName4 = findViewById(R.id.tvRankName4);
        tvRankName5 = findViewById(R.id.tvRankName5);
        tvScore1 = findViewById(R.id.tvScore1);
        tvScore2 = findViewById(R.id.tvScore2);
        tvScore3 = findViewById(R.id.tvScore3);
        tvScore4 = findViewById(R.id.tvScore4);
        tvScore5 = findViewById(R.id.tvScore5);
    }

    /**
     * Fetches user data from the Firebase Realtime Database and displays the top players.
     * This method retrieves all users, sorts them by their score in descending order,
     * and then updates the TextViews to show the names and scores of the top 5 players.
     * If there are fewer than 5 players, it will display as many as are available.
     * It also handles potential errors during the database fetch operation.
     */
    @SuppressLint("SetTextI18n")
    private void displayLeaderboard() {
        var usersDBRef = FirebaseDatabase.getInstance().getReference("Users");
        usersDBRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                System.out.println("Error getting data");
                return;
            }

            var snapshot = task.getResult();
            if (snapshot == null) {
                return;
            }

            List<User> userList = new ArrayList<>();
            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                User user = userSnapshot.getValue(User.class);
                if (user != null) {
                    userList.add(user);
                }
            }

            // Sort the list of users by highest score
            userList.sort((u1, u2) -> Integer.compare(u2.getHighestScore(), u1.getHighestScore()));

            // Get the top 5 users
            TextView[] nameTextViews = {tvRankName1, tvRankName2, tvRankName3, tvRankName4, tvRankName5};
            TextView[] scoreTextViews = {tvScore1, tvScore2, tvScore3, tvScore4, tvScore5};


            int loopLimit = Math.min(userList.size(), 5);
            // Set the text for the corresponding rank
            IntStream.range(0, loopLimit).forEach(i -> {
                User topUser = userList.get(i);
                nameTextViews[i].setText((i + 1) + ". " + topUser.getUserName());
                scoreTextViews[i].setText(valueOf(topUser.getHighestScore()));
            });


        });
    };
}