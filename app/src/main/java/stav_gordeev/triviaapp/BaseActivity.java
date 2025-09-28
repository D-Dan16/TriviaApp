package stav_gordeev.triviaapp;

import static java.util.List.of;

import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class BaseActivity extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (List.of(
            R.id.mnuSettings,
            R.id.mnuUserProperties,
            R.id.mnuInstructions,
            R.id.mnuAbout,
            R.id.mnuMusic
        ).contains(itemId)) return true;
        else return super.onOptionsItemSelected(item);
    }
}
