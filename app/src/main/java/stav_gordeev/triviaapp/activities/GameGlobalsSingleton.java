package stav_gordeev.triviaapp.activities;

import java.util.ArrayList;
import java.util.List;

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
    private List<Question> questionList;
    private final int levelsInGame;
    private User currentUser;

    private GameGlobalsSingleton(){
        questionList= new ArrayList<>();
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

    public List<Question> getQuestionList(){
        return questionList;
    }

    public void setQuestionList(List list){
        questionList = list;
    }

    public int getLevelsInGame(){
        return levelsInGame;
    }

    public void clearQuestionList(){
        questionList.clear();
    }
}