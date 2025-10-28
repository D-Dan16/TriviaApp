package stav_gordeev.triviaapp.activities;

import static stav_gordeev.triviaapp.R.*;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

import stav_gordeev.triviaapp.R;

/**
 * A base activity class that provides a common options menu for all activities that extend it.
 * This class handles the creation and item selection of a standard menu,
 * which includes options like Settings, User Properties, Instructions, About, and Music control.
 * Activities extending this class will automatically inherit this menu functionality.
 */
public class BaseActivity extends AppCompatActivity {
    /**
     * Sets the activity's content from a layout resource. The resource will be
     * inflated, adding all top-level views to the activity.
     * <p>
     *
     * @param layoutResID The resource ID of the layout that will be the content of the activity.
     */
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setupToolbar();
    }

    /**
     * Initializes and sets up the toolbar for the activity.
     * This method finds the toolbar view by its ID, and if it exists,
     * sets it as the activity's action bar. This allows the toolbar
     * to host the options menu and other action items.
     */
    protected void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();


        switch (itemId) {
            case R.id.mnuUserProperties:
                startActivity(new Intent(this, EditUserProperties.class));
                return true;
            case R.id.mnuToGame:
                startActivity(new Intent(this, Game.class));
                return true;
            case R.id.mnuLeaderboard:
                startActivity(new Intent(this, Leaderboard.class));
                return true;
            case R.id.mnuDeleteUser:
                startActivity(new Intent(this, DeleteUser.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
}
