package stav_gordeev.triviaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

import stav_gordeev.triviaapp.Helpers.MusicService;
import stav_gordeev.triviaapp.Helpers.TriviaQuestionGenerator;
import stav_gordeev.triviaapp.R;

public class GameOver extends BaseActivity {

    LottieAnimationView lavGameOver;
    TextView tvPointsGO,tvAllScores;
    private Button bRetry;
    private Button bToMainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_over);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Pause Music ---
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("STOP");
        startService(playIntent);

        tvPointsGO=findViewById(R.id.tvPointsGO);
        tvAllScores=findViewById(R.id.tvAllScores);
        bRetry = findViewById(R.id.bRetry);
        bToMainMenu = findViewById(R.id.bToMainMenu);

        Intent intent=getIntent();
        int points = intent.getIntExtra("points",0);
        
        tvPointsGO.setText(Integer.toString(points));


        // Start loading in a new set of questions for the player to play!
        TriviaQuestionGenerator.createQuestionListInBackground(this);


        registerButtons();

    }

    private void registerButtons() {
        bRetry.setOnClickListener(v->{
            if (GameGlobalsSingleton.getInstance().getQuestionList().isEmpty()) {
                Toast.makeText(this, "Wait a little more until the questions are ready to be presented ", Toast.LENGTH_LONG).show();
                return;
            }

            Intent toGame = new Intent(this, Game.class);
            startActivity(toGame);
        });

        bToMainMenu.setOnClickListener(v->{
            Intent toMenu = new Intent(this, MainActivity.class);
            startActivity(toMenu);
        });
    }


}