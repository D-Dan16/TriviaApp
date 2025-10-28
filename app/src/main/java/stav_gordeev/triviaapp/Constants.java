package stav_gordeev.triviaapp;

public class Constants {
    public final static int timeForQuestion = 10 * 1000;
    public final static int numOfQuestions = 10;

    public final static String geminiPrompt =
            "Generate " + numOfQuestions + """
                    random general-knowledge trivia questions. The output must be a single JSON object with a key named trivia_questions. The value of trivia_questions must be a JSON array. Each element of the array must be a JSON object representing a question, containing exactly four keys: question, correct_answer, wrong_answer_1, wrong_answer_2, and wrong_answer_3. STRICTLY DO NOT include any text, explanation, introductory/concluding words, or markdown formatting (like ```json) outside of the single JSON object.\s
                    """;
}