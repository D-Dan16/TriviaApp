package stav_gordeev.triviaapp.activities;

import java.util.ArrayList;

import stav_gordeev.triviaapp.Helpers.Question;

// Add this interface inside the GameGlobalsSingleton class or in its own file
public interface QuestionsCallback {
    void onCallback(ArrayList<Question> questions);
}
