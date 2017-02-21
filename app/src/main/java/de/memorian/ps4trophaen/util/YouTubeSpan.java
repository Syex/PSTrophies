package de.memorian.ps4trophaen.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.style.URLSpan;
import android.view.View;

/**
 * A span that starts the YouTubeDialogActivity to watch YouTube videos.
 *
 * @since 21.12.2014 $Id%
 */
public class YouTubeSpan extends URLSpan {

    private String videoID;

    public YouTubeSpan(String url) {
        super(url);
        parseVideoID();
    }

    @Override
    public void onClick(View widget) {
        Context context = widget.getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean playIntern = sharedPref.getBoolean("play_youtube_intern", true);

        if(isAppInstalled("com.google.android.youtube", context) && playIntern) {
            Intent youTubeIntent = new Intent(context, YouTubeDialogActivity.class);
            youTubeIntent.putExtra("video_id", videoID);
            context.startActivity(youTubeIntent);
        }
        else {
            super.onClick(widget);
        }
    }

    private void parseVideoID() {
        String url = getURL();
        int i = url.indexOf("v=");
        if (url.contains("&index=")) {
            int j = url.indexOf("&index=");
            videoID = url.substring(i + 2, j);
        } else {
            videoID = url.substring(i + 2, url.length());
        }
    }

    protected boolean isAppInstalled(String packageName, Context context) {
        Intent mIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null) {
            return true;
        }
        else {
            return false;
        }
    }
}
