package de.memorian.ps4trophaen;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.memorian.ps4trophaen.storage.GameOverviewDBHelper;
import de.memorian.ps4trophaen.storage.TrophyDBHelper;
import de.memorian.ps4trophaen.tasks.GetGameOverviewTask;

/**
 * Dialog activity that shows the progress of the syncronization.
 *
 * @author Tom-Philipp Seifert
 * @since 04.11.2014
 */
public class DBSyncActivity extends Activity {

    /**
     * URL to mod date PHP script.
     */
    public static final String versionURL = "http://www.seifertion.de/PS4Server/getmoddate.php";
    private int gamesFinished = 0;
    private int gamesAmount = 0;
    private TextView progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dbsync);
        setFinishOnTouchOutside(false);
        progressView = (TextView) findViewById(R.id.syncDBText);
        syncDB();
    }

    private void syncDB() {
        GetGameOverviewTask getGameOverviewTask =
                new GetGameOverviewTask(this,
                        GameOverviewDBHelper.getInstance(this),
                        TrophyDBHelper.getInstance(this));
        getGameOverviewTask.execute((Void) null);
    }

    public void setGamesAmount(int i) {
        gamesAmount = i;
        setGameProgress();
    }

    public void gameFinished() {
        gamesFinished++;
        setGameProgress();
    }

    public void syncFinished() {
        setDBVersion();
        setResult(RESULT_OK);
        finish();
    }

    private void setDBVersion() {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream is = null;
                    try {
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = new HttpPost(versionURL);
                        HttpResponse response = httpclient.execute(httppost);
                        HttpEntity entity = response.getEntity();
                        is = entity.getContent();

                    } catch (Exception e) {
                        Log.e("log_tag", "Error in http connection " + e.toString(), e);
                    }

                    // Convert response to string
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        StringBuilder sb = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        is.close();
                        String date = sb.toString();
                        getSharedPreferences(MainActivity.SETTINGS_FILE, MODE_PRIVATE)
                                .edit()
                                .putString(MainActivity.DB_VERSION_DATE, date)
                                .commit();
                    } catch (Exception e) {
                        Log.e("log_tag", "Error converting result " + e.toString(), e);
                    }
                }
            });
            t.start();
    }

    private void setGameProgress() {
        if (progressView != null) {
            progressView.setText(gamesFinished + " / " + gamesAmount);
        }
    }

}
