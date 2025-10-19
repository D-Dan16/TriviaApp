
package stav_gordeev.triviaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import stav_gordeev.triviaapp.R;

public class Register extends BaseActivity {

    private FloatingActionButton fabCancelReg;
    private FloatingActionButton fabOKReg;
    private EditText etNameReg;
    private EditText etEmailReg;
    private EditText etPhone;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();


        fabCancelReg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                Intent intent=new Intent( Register.this, MainActivity.class);
                startActivity(intent);
			}
        });
    }
    
    public void init() {
            fabCancelReg=findViewById(R.id.fabCancelReg);
            fabOKReg=findViewById(R.id.fabOkReg);
            etNameReg=findViewById(R.id.etNameReg);
            etEmailReg=findViewById(R.id.etEmailReg);
            etPhone=findViewById(R.id.etPhone);
            etPassword=findViewById(R.id.etPassword);
    }
}