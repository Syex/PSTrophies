package de.memorian.ps4trophaen.util;

import android.os.Bundle;
import android.view.Window;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import de.memorian.ps4trophaen.R;

/**
 * An activity to show a YouTube video instead of in the YouTube app.
 *
 * @since 21.12.2014
 */
public class YouTubeDialogActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener {

    public static final String API_KEY = "AIzaSyBDv1EdcCzlTPvO0ZyNDq8xaoX_iuKxEuI";
    public static String VIDEO_ID;
    private YouTubePlayer youTubePlayer;
    private YouTubePlayerView youTubePlayerView;
    private static final int RQS_ErrorDialog = 1;
    private MyPlayerStateChangeListener myPlayerStateChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.youtube_dialog_activity);
        Bundle bundle = getIntent().getExtras();
        VIDEO_ID = bundle.getString("video_id");
        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubePlayerView.initialize(API_KEY, this);
        myPlayerStateChangeListener = new MyPlayerStateChangeListener();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        youTubePlayer = player;
        youTubePlayer.setFullscreen(true);
        youTubePlayer.setPlayerStateChangeListener(myPlayerStateChangeListener);
        if (!wasRestored) {
            player.cueVideo(VIDEO_ID);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, RQS_ErrorDialog).show();
        }
    }

    private final class MyPlayerStateChangeListener implements
            YouTubePlayer.PlayerStateChangeListener {
        @Override
        public void onAdStarted() {
        }

        @Override
        public void onError(
                com.google.android.youtube.player.YouTubePlayer.ErrorReason arg0) {
        }

        @Override
        public void onLoaded(String arg0) {
        }

        @Override
        public void onLoading() {
        }

        @Override
        public void onVideoEnded() {
            finish();
        }

        @Override
        public void onVideoStarted() {
        }
    }
}
