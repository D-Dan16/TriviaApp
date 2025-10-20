package stav_gordeev.triviaapp.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import stav_gordeev.triviaapp.R;

public class ExpIntro extends BaseActivity {

    private TextView tvInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exp_intro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        String all=readIntroductionsFromFile();
        tvInstructions.setText(all);
    }

    public void init()
    {
        tvInstructions=findViewById(R.id.tvInstructions);
    }



    public String readIntroductionsFromFile()
    {
        InputStream is;
        InputStreamReader isr;
        BufferedReader br;
        is=getResources().openRawResource(R.raw.instructions);
        isr=new InputStreamReader(is);
        br=new BufferedReader(isr);
        String all="";
        String st;
        try {
            st=br.readLine();
            while (st!=null) {
                all+=st+"\n";
                st=br.readLine();
            }
            br.close();
        }
        catch (IOException e) {
            Toast.makeText(this,"could not open", Toast.LENGTH_SHORT).show();
        }
        return all;
    }
}