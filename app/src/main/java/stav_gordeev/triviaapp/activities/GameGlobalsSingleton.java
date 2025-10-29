package stav_gordeev.triviaapp.activities;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import stav_gordeev.triviaapp.Constants;
import stav_gordeev.triviaapp.Helpers.Question;
import stav_gordeev.triviaapp.Helpers.User;

/**
 * A Singleton class to hold global game data that needs to be accessible across different activities.
 * This includes the current user's data, the list of trivia questions for the current game,
 * and the total number of levels (questions) in a game session.
 * The Singleton pattern ensures that only one instance of this class exists throughout the application's lifecycle,
 * providing a centralized point of access to shared game state.
 */
public class GameGlobalsSingleton {
    private final int levelsInGame;
    public boolean hasQuestions = false;
    private User currentUser;

    private GameGlobalsSingleton(){
        levelsInGame = Constants.numOfQuestions;
    }

    private static class SingletonHelper{
        private static final GameGlobalsSingleton INSTANCE = new GameGlobalsSingleton();
    }

    public User getCurrentUser(){
        return currentUser;
    }
    public void setCurrentUser(User user){
        currentUser = user;
    }

    public static GameGlobalsSingleton getInstance(){
        return SingletonHelper.INSTANCE;
    }

    /**
     * Synchronously fetches the list of questions from the Firebase Realtime Database.
     * WARNING: This method BLOCKS the calling thread until the network operation is complete.
     * It MUST NOT be called from the Android Main/UI thread, or the application will freeze and crash.
     *
     * @return An ArrayList of Question objects, or an empty list if the fetch fails.
     */
    public ArrayList<Question> getQuestionList() {
        DatabaseReference questionsRef = FirebaseDatabase.getInstance().getReference("Questions");
        Task<DataSnapshot> task = questionsRef.get();
        ArrayList<Question> questionList = new ArrayList<>();

        try {
            // Block the current thread and wait for the task to complete.
            DataSnapshot snapshot = Tasks.await(task);

            if (snapshot.exists()) {
                // Loop through the children (the individual questions) in the snapshot
                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    // Convert each snapshot into a Question object
                    Question question = questionSnapshot.getValue(Question.class);
                    if (question != null) {
                        questionList.add(question);
                    }
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            // Restore the interrupted status and log the error.
            Thread.currentThread().interrupt();
            System.out.println("Error fetching questions synchronously: " + e.getMessage());
        }

        return questionList;
    }


    public int getLevelsInGame(){
        return levelsInGame;
    }
    public void clearQuestionList(){
        FirebaseDatabase.getInstance().getReference("Questions").removeValue();
        hasQuestions = false;
    }
}

