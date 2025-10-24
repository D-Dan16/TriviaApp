package stav_gordeev.triviaapp.Helpers;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.Objects;

import stav_gordeev.triviaapp.R;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.game_music );
        mediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        switch (Objects.requireNonNull(action)) {
            case "PLAY" -> {
                createNotification();
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
            case "PAUSE" -> {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }
            case "STOP" -> {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.prepareAsync();
                }
                stopSelf();
            }
        }

        return START_STICKY;
    }

    private void createNotification() {
        new NotificationCompat.Builder(this, "music_channel")
                .setContentTitle("playing music")
                .setContentText("playing right now")
                .setSmallIcon(R.drawable.ic_music_note)
                .addAction(R.drawable.ic_pause, "pause", getPendingIntent("PAUSE"))
                .addAction(R.drawable.ic_stop, "stop", getPendingIntent("STOP"))
                .build();
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
