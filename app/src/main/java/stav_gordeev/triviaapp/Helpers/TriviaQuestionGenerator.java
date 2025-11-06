package stav_gordeev.triviaapp.Helpers;

import android.content.Context;import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import stav_gordeev.triviaapp.Constants;
import stav_gordeev.triviaapp.R;

/**
 * A helper class responsible for generating a list of trivia questions using the Gemini AI.
 * This class handles the asynchronous request to the generative model, parses the JSON response,
 * and populates a list of {@link Question} objects. The generated list is then stored in the
 * {@link GameGlobalsSingleton} for application-wide access.
 */
public class TriviaQuestionGenerator {

    private static final String TAG = "TriviaQuestionGenerator";

    /**
     * Asynchronously generates a list of trivia questions using the Gemini AI model.
     *
     * <p>This method runs on a background thread to fetch data from the Gemini API,
     * parse the JSON response, and create a list of {@link Question} objects.
     * The resulting list is then stored in the {@link GameGlobalsSingleton}.
     * Any errors during this process are logged.
     *
     * @param context The application context, used for accessing resources like the API key.
     */
    public static void createQuestionListInBackground(Context context) {
        //TODO: in order to not spam the AI. temp
//        if (true) return;

        Thread backgroundThread = new Thread(() -> {
            try {
                GenerativeModelFutures model = GenerativeModelFutures.from(new GenerativeModel(
                        "gemini-2.5-flash",
                        context.getString(R.string.API_KEY)
                ));

                Content prompt = new Content.Builder()
                        .addText(Constants.geminiPrompt)
                        .build();

                ListenableFuture<GenerateContentResponse> futureResponse = model.generateContent(prompt);

                String output = futureResponse.get().getText();

                JSONArray arr = new JSONObject(output).getJSONArray("trivia_questions");

                List<Question> questionList = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Question q = new Question(
                            i,
                            obj.getString("question"),
                            obj.getString("correct_answer"),
                            obj.getString("wrong_answer_1"),
                            obj.getString("wrong_answer_2"),
                            obj.getString("wrong_answer_3")
                    );
                    questionList.add(q);
                }

                GameGlobalsSingleton.getInstance().setQuestionList(questionList);

            } catch (Exception e) {
                Log.e(TAG, "Error creating question list", e);
            }
        });
        backgroundThread.start();
    }
}
