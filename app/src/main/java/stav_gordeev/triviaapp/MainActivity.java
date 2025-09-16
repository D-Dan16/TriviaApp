package stav_gordeev.triviaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
        private FloatingActionButton fabRegister;
        private FloatingActionButton fabLogin;
        private CheckBox cbPersonal;
        private EditText etEmail;
        private EditText etPassword;

        // Preload questions that'll be used for Game Activity
        public ArrayList<Question> questions;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            init();
            loadLastLoggedInUserData();


            fabRegister.setOnClickListener(view -> {
                Intent intent=new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            });

            fabLogin.setOnClickListener(view -> {
                //perform login
                if (verifyUserExist()) {
                    if (cbPersonal.isChecked())
                    {
                        // save email and password in shared preferences
                        saveLoggedInUserInSharedPreferences();
                    }
                    Intent intent = new Intent(MainActivity.this, Game.class);
                    intent.putExtra("email",etEmail.getText().toString());
                    intent.putExtra("questions",questions);
                    startActivity(intent);
                }
            });
        }


    //TODO: For now, this always returns true
    private boolean verifyUserExist() {
        return true;
    }

    @SuppressLint("CutPasteId")
        public void init() {
            fabRegister=findViewById(R.id.fabOkReg);
            fabLogin=findViewById(R.id.fabOkReg);
            etEmail=findViewById(R.id.etEmailReg);
            etPassword=findViewById(R.id.etPassword);
            cbPersonal=findViewById(R.id.cbPersonal);
            loadQuestions();
        }


    public void loadQuestions()
    {
        questions=new ArrayList<>();
        String q="why";
        String a1="1";
        String a2="3";
        String a3="4";
        String a4="4";
        String c="4";
        Question question1=new Question(q, a1, a2, a3, a4, c);
        questions.add(question1);
        q="when";
        a1="a";
        a2="b";
        a3="c";
        a4="d";
        c="a";
        Question question2=new Question(q, a1, a2, a3, a4, c);
        questions.add(question2);
    }

    public void loadLastLoggedInUserData()
    {
        SharedPreferences sp=
                getSharedPreferences("trivia",MODE_PRIVATE);
        String email = sp.getString("email", ""); // Provide a default value in case the key is not found\
        String password = sp.getString("password", ""); // Provide a default value in case the key is not found
        etEmail.setText(email);
        etPassword.setText(password);


    }

    public void saveLoggedInUserInSharedPreferences()
    {
        SharedPreferences sp=
                getSharedPreferences("trivia",MODE_PRIVATE);
        SharedPreferences.Editor editor= sp.edit();
        editor.putString("email",etEmail.getText().toString());
        editor.putString("password",etPassword.getText().toString());
        editor.apply();
    }





}
