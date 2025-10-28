package stav_gordeev.triviaapp.Helpers;

import android.content.Context;import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import stav_gordeev.triviaapp.Constants;
import stav_gordeev.triviaapp.R;
import stav_gordeev.triviaapp.activities.GameGlobalsSingleton;

public class TriviaQuestionGenerator {

    private static final String TAG = "TriviaQuestionGenerator";
    private static DatabaseReference questionsDBRef;

    public static void createQuestionListInBackground(Context context) {
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

                questionsDBRef = FirebaseDatabase.getInstance().getReference("Questions");

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
                    DatabaseReference questionNode = questionsDBRef.child("question_"+(i+1));
                    questionNode.setValue(q);
                }

                GameGlobalsSingleton.getInstance().hasQuestions = true;

            } catch (Exception e) {
                Log.e(TAG, "Error creating question list", e);
            }
        });
        backgroundThread.start();
    }
}
