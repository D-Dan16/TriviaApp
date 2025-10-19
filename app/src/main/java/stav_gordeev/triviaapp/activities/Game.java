package stav_gordeev.triviaapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import stav_gordeev.triviaapp.Question;
import stav_gordeev.triviaapp.R;

public class Game extends BaseActivity {
    //region Fields
    public ImageView ivTimer;
    public TextView tvTimer;
    public ImageView ivQuestion;
    public TextView tvQuestion;
    public Button btnOption1;
    public TextView tvAnswer1;
    public Button btnOption2;
    public TextView tvAnswer2;
    public Button btnOption3;
    public TextView tvAnswer3;
    public Button btnOption4;
    public TextView tvAnswer4;
    public ProgressBar pbQuestion;
    public TextView tvProgress;

    public Question current;
    public int points;
    public int rounds=0;
    public int MAX_ROUNDS;
    public String email;
    private ArrayList<Question> questions;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();

        Intent intent=getIntent();
        email=intent.getStringExtra("email");
        questions= (ArrayList<Question>) intent.getSerializableExtra("questions");

        assert questions != null;
        MAX_ROUNDS=questions.size();

        tvProgress.setText(rounds +"/"+ MAX_ROUNDS);


        getRandomQuestion();
    }

    private void init()
    {
        tvTimer=findViewById(R.id.tvTimer);
        tvQuestion=findViewById(R.id.tvQuestion);
        btnOption1=findViewById(R.id.btnOp1);
        tvAnswer1=findViewById(R.id.tvAnswer1);
        btnOption2=findViewById(R.id.btnOp2);
        tvAnswer2=findViewById(R.id.tvAnswer2);
        btnOption3=findViewById(R.id.btnOp3);
        tvAnswer3=findViewById(R.id.tvAnswer3);
        btnOption4=findViewById(R.id.btnOp4);
        tvAnswer4=findViewById(R.id.tvAnswer4);
        pbQuestion=findViewById(R.id.pbQuestion);
        tvProgress=findViewById(R.id.tvProgress);
    }



    @SuppressLint("SetTextI18n")
    public boolean getRandomQuestion()
    {
        int numQuestions=questions.size();
        if (numQuestions>0&&rounds<MAX_ROUNDS) {
            rounds++;
            pbQuestion.setProgress(rounds);
            tvProgress.setText(rounds +"/"+ MAX_ROUNDS);
            int min = 0;
            int max = numQuestions - 1;
            int randomize = min + (int) (Math.random() * (max - min + 1));
            current = questions.get(randomize);
            questions.remove(randomize);
            tvQuestion.setText(current.getQuestion());
            tvAnswer1.setText(current.getPossibleAnswer1());
            tvAnswer2.setText(current.getPossibleAnswer2());
            tvAnswer3.setText(current.getPossibleAnswer3());
            tvAnswer4.setText(current.getPossibleAnswer4());
            return true;
        }
        else {
            return  false;
        }
    }


    public void SelectAnswer (View v) {
        if (v.getId() == R.id.btnOp1) {
            if (current.getCorrectAnswer().equals(current.getPossibleAnswer1())) {
                // correct
                points++;
                notifySuccess();
            } else {
                notifyFailure();
            }
        } else if (v.getId() == R.id.btnOp2) {
            if (current.getCorrectAnswer().equals(current.getPossibleAnswer2())) {
                // correct
                points++;
                notifySuccess();
            } else {
                notifyFailure();
            }
        } else if (v.getId() == R.id.btnOp3) {
            if (current.getCorrectAnswer().equals(current.getPossibleAnswer3())) {
                // correct
                points++;
                notifySuccess();
            } else {
                notifyFailure();
            }
        } else if (v.getId() == R.id.btnOp4) {
            if (current.getCorrectAnswer().equals(current.getPossibleAnswer4())) {
                // correct
                points++;
                notifySuccess();
            } else {
                notifyFailure();
            }
        }
    }

    public void notifySuccess()
    {
        AlertDialog.Builder adbCorrectResponse;
        adbCorrectResponse = new AlertDialog.Builder(Game.this);
        adbCorrectResponse.setTitle("Correct");
        adbCorrectResponse.setMessage("well done");
        adbCorrectResponse.setCancelable(false);
        adbCorrectResponse.setIcon(R.drawable.outline_check_24);
        adbCorrectResponse.setNeutralButton("OK", (dialog, which) -> goToNext());
        adbCorrectResponse.create().show();

    }
    public void notifyFailure() {
        AlertDialog.Builder adbCorrectResponse;
        adbCorrectResponse = new AlertDialog.Builder(Game.this);
        adbCorrectResponse.setTitle("Incorrect");
        adbCorrectResponse.setMessage("Better luck next time");
        adbCorrectResponse.setCancelable(false);
        adbCorrectResponse.setIcon(R.drawable.outline_close_24);
        adbCorrectResponse.setNeutralButton("OK", (dialog, which) -> goToNext());
        adbCorrectResponse.create().show();
    }

    public void goToNext()
    {
        if (!getRandomQuestion())
        {
            //game over
            Intent intent=new Intent(Game.this, GameOver.class);
            intent.putExtra("points",points);
            intent.putExtra("email",email);
            startActivity(intent);
        }
    }
}