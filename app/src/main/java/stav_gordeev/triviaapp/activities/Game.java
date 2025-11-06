package stav_gordeev.triviaapp.activities;

import static java.lang.String.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;


import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;

import stav_gordeev.triviaapp.Constants;
import stav_gordeev.triviaapp.Helpers.GameGlobalsSingleton;
import stav_gordeev.triviaapp.Helpers.MusicService;
import stav_gordeev.triviaapp.Helpers.Question;
import stav_gordeev.triviaapp.R;
import stav_gordeev.triviaapp.Helpers.User;


/**
 * Represents the main game screen activity where the trivia game is played.
 * This activity is responsible for:
 * - Displaying trivia questions and their multiple-choice answers.
 * - Managing a countdown timer for each question.
 * - Handling user's answer selection and submission.
 * - Keeping track of the user's score and progress through the game.
 * - Providing visual feedback for correct and incorrect answers.
 * - At the end of the game, updating the user's stats (high score, total games played)
 *   in Firebase Realtime Database.
 * - Navigating to the GameOver activity upon game completion.
 * - Controlling background music playback via MusicService.
 *
 * The questions for the game are retrieved from the {@link GameGlobalsSingleton}.
 */
public class Game extends BaseActivity {

    private static final String TAG = "GameActivity"; // For logging

    // Using ArrayList to store the Question objects
    private List<Question> questionList; // Declare the list
    private List<Question> tempQuestionsHolder; // A temporary list to hold questions while fetching
    private final boolean initialLoadDone = false; // Flag to ensure initial load happens only once
    // private int levelsSuccessfullyFetched = 0; // Counter for successfully fetched levels

    // UI elements
    ChipGroup cgAnswers;
    Chip cAns1, cAns2, cAns3, cAns4;
    TextView tvQuestion;
    Button bSubmit;
    ImageView ivCorrect, ivWrong;

    // Game Logic Variables
    int correctAnswer = 0, selectedAnswer = 0;
    int correctAnswersCounter = 0;    // how many correct answers by user. keep score.
    int currentQuestionIndex = 0;
    int questionsInGame = GameGlobalsSingleton.getInstance().getLevelsInGame();
    // this should also be put as GameGlobal !!
    String currentCategory = "SolarSystem";
    private TextView tvProgress;
    private TextView tvTimer;
    private CountDownTimer curTimerCountdown;
    private long timeForQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // --- Play Music ---
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("PLAY");
        startService(playIntent);

        initUI();

        // Initialize questionList
        questionList = GameGlobalsSingleton.getInstance().getQuestionList();

        // TODO: Temp the timer will be a fixed num that can't be configurable
        timeForQuestion = Constants.timeForQuestion;

        startCountDownTimer();


        loadNextQuestion();


        sendAnswerButtonLogic();
    }

    /**
     * Initializes and starts a countdown timer for the current question.
     * The timer updates a TextView every second to show the remaining time.
     * If the timer finishes before the user answers, it displays a "Time's Up!" message,
     * advances to the next question, and calls {@link #loadNextQuestion()} to display it.
     */
    private void startCountDownTimer() {
        curTimerCountdown = new CountDownTimer(timeForQuestion, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Time Left: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                Toast.makeText(Game.this, "Time's Up!", Toast.LENGTH_SHORT).show();
                currentQuestionIndex++;
                loadNextQuestion();
            }
        }.start();
    }

    /**
     * Binds the logic for submitting a user's answer. When the user submits,
     * it validates an answer has been chosen, processes the answer for correctness,
     * provides visual feedback, and proceeds to the next question. If no
     * answer is selected, it prompts the user to choose one.
     */
    private void sendAnswerButtonLogic() {
        bSubmit.setOnClickListener(v -> {
            // see that any chip is checked
            if (!cgAnswers.getCheckedChipIds().isEmpty()) {
                // set selected answer
                selectedAnswer = chipIdToInt(cgAnswers.getCheckedChipId());
                // check if correct
                if (selectedAnswer == correctAnswer) {
                    correctAnswersCounter++;
                    showIfCorrect(ivCorrect);
                } else
                    showIfCorrect(ivWrong);

                currentQuestionIndex++;
                loadNextQuestion();
            } else
                Toast.makeText(this, "Pick one!", Toast.LENGTH_SHORT).show();
        });
    }

    private void showIfCorrect(ImageView iv) {
        iv.setVisibility(View.VISIBLE);
        iv.setScaleX(2);
        iv.setScaleY(2);
        new Handler().postDelayed(() -> {
            iv.setVisibility(View.INVISIBLE);
            iv.setScaleX(1);
            iv.setScaleY(1);
        }, 1000);
    }

    /**
     * Converts a Chip's resource ID to a zero-based integer index.
     * This is used to map the selected answer Chip to a corresponding index in the answers list.
     *
     * @param chipId The resource ID of the selected Chip from the ChipGroup.
     * @return An integer from 0 to 3 representing the Chip's position, or -1 if the ID is not recognized.
     */
    private int chipIdToInt(int chipId) {
        if (chipId == R.id.cAns1)
            return 0;
        else if (chipId == R.id.cAns2)
            return 1;
        else if (chipId == R.id.cAns3)
            return 2;
        else if (chipId == R.id.cAns4)
            return 3;
        else
            return -1;
    }

    @Override
    protected void onStop() {
        super.onStop();
        curTimerCountdown.cancel();
    }

    /**
     * Manages the game's question flow. It checks if there are questions available and if the game
     * should continue. If the game is ongoing, it calls {@link #showNextQuestion()} to display
     * the next question and restarts the countdown timer. If the game is over (all questions have been
     * answered), it updates the user's statistics (high score, total games played) in both the local
     * singleton and the Firebase Realtime Database. Finally, it navigates to the GameOver activity,
     * passing the final score.
     */
    @SuppressLint("DefaultLocale")
    private void loadNextQuestion() {
        if (questionList.isEmpty()) {
            Log.e(TAG, "loadNextQuestion called but questionList is empty.");
            //  "no questions" state
            tvQuestion.setText(R.string.oops_no_questions_found);
            bSubmit.setEnabled(false);
            for (int i = 0; i < cgAnswers.getChildCount(); i++)
                cgAnswers.getChildAt(i).setEnabled(false);
            return;
        }

        curTimerCountdown.cancel();

        // check what question is now
        if (currentQuestionIndex < questionsInGame) {
            showNextQuestion();
            startCountDownTimer();
            return;
        }


        //region Game Over
        User curUser = GameGlobalsSingleton.getInstance().getCurrentUser();

        // here confetti animation
        if (correctAnswersCounter > curUser.getHighestScore()) {
            // Toast message
            Toast.makeText(this, "New High Score of " + correctAnswersCounter + "!", Toast.LENGTH_LONG).show();
            // set in the GameGlobals user object
            curUser.setHighestScore(correctAnswersCounter);
        } else {
            Toast.makeText(this, "Game Over You got " + correctAnswersCounter + " out of " + questionsInGame + " correct!", Toast.LENGTH_LONG).show();
        }
        // in any case update that a game was played

        curUser.setTotalGamesPlayed(curUser.getTotalGamesPlayed() + 1);
        // we also want to update this info in the Firebase Realtime Database
        Log.d(TAG, "Updating user info in Firebase uid" + curUser.getUid());
        // this is the Firebase Database Object
        // Firebase references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // this is the reference to the specific user node in the database
        DatabaseReference userRef = database.getReference("Users").child(curUser.getUid());
        // set the up to date values from game globals
        userRef.child("highestScore").setValue(curUser.getHighestScore());
        userRef.child("totalGamesPlayed").setValue(curUser.getTotalGamesPlayed());


        // Go to Game Over Activity
        Intent gameOverIntent = new Intent(this, GameOver.class);
        gameOverIntent.putExtra("correctAnswersCounter", correctAnswersCounter);
        startActivity(gameOverIntent);
        finish();

        //endregion
    }

    /**
     * Displays the next question and its possible answers on the screen.
     * It updates the progress text view, clears any previous answer selections,
     * and enables the submit button. It retrieves the current question from the
     * question list based on the {@code currentQuestionIndex}, scrambles the answers
     * to randomize their positions, and then sets the question text and answer chips.
     * The correct answer's position is determined by the {@link #scrambleAnswers(Question)} method.
     */
    @SuppressLint("DefaultLocale")
    private void showNextQuestion() {
        tvProgress.setText(format("%d/%d", currentQuestionIndex + 1, questionsInGame));


        // unchecks all chips
        cgAnswers.clearCheck();
        bSubmit.setEnabled(true);

        // retrieve next question
        Question currentQuestion = questionList.get(currentQuestionIndex);
        // here scramble answers function
        List<String> answers = scrambleAnswers(currentQuestion);
        // set question text and answers
        tvQuestion.setText(currentQuestion.getQueText());
        cAns1.setText(answers.get(0));
        cAns2.setText(answers.get(1));
        cAns3.setText(answers.get(2));
        cAns4.setText(answers.get(3));
    }

    private List<String> scrambleAnswers(Question question) {
        Random random = new Random();
        correctAnswer = random.nextInt(4);

        List<String> answers = new ArrayList<>();
        String correct = question.getAnsCorrect();
        answers.add(question.getAnsWrong1());
        answers.add(question.getAnsWrong2());
        answers.add(question.getAnsWrong3());

        Collections.shuffle(answers);
        answers.add(correctAnswer, correct);
        return answers;
    }

    private void initUI() {
        tvTimer = findViewById(R.id.tvTimer);
        tvProgress = findViewById(R.id.tvProgress);
        cgAnswers = findViewById(R.id.cgAnswers);
        cAns1 = findViewById(R.id.cAns1);
        cAns2 = findViewById(R.id.cAns2);
        cAns3 = findViewById(R.id.cAns3);
        cAns4 = findViewById(R.id.cAns4);
        tvQuestion = findViewById(R.id.tvQuestion);
        bSubmit = findViewById(R.id.bSubmit);
        bSubmit.setEnabled(false);
        ivCorrect = findViewById(R.id.ivCorrect);
        ivWrong = findViewById(R.id.ivWrong);
    }
}