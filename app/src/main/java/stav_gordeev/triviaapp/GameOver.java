package stav_gordeev.triviaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

public class GameOver extends BaseActivity {

    LottieAnimationView lavGameOver;
    TextView tvUserScore,tvPointsGO,tvAllScores;
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

        init();
        Intent intent=getIntent();
        int points = intent.getIntExtra("points",0);
        String email=intent.getStringExtra("email");

        tvPointsGO.setText(Integer.toString(points));
        tvUserScore.setText(email);

    }

    public void init()
    {
        tvPointsGO=findViewById(R.id.tvPointsGO);
        tvUserScore=findViewById(R.id.tvUserScore);
        tvAllScores=findViewById(R.id.tvAllScores);
    }


}