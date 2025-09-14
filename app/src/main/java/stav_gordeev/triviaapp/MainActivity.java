package stav_gordeev.triviaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
        private FloatingActionButton fabRegister;
        private FloatingActionButton fabLogin;
        private CheckBox cbPersonal;
        private EditText etEmail;
        private EditText etPassword;

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

            fabRegister.setOnClickListener(view -> {
                Intent intent=new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            });
        }

        @SuppressLint("CutPasteId")
        public void init()
        {
        fabRegister=findViewById(R.id.fabOkReg);
        fabLogin=findViewById(R.id.fabOkReg);
        etEmail=findViewById(R.id.etEmailReg);
        etPassword=findViewById(R.id.etPassword);

        }



}
