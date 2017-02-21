package de.memorian.ps4trophaen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.memorian.showcaseview.OnShowcaseEventListener;
import de.memorian.showcaseview.ShowcaseView;
import de.memorian.showcaseview.targets.ActionItemTarget;


public class MainActivity extends Activity implements OnShowcaseEventListener {

    public static final String DB_VERSION_DATE = "dbVersion";
    public static final String FAVORITES_FILE = "favorites";
    public static final String TITLE_FONT = "fonts/Exo2-Light.otf";
    public static final String HIDDEN_TROPHY_FONT = "fonts/Exo2-LightItalic.otf";
    public static final String TEXT_FONT = "fonts/Exo2-Medium.otf";
    public static final String SETTINGS_FILE = "settings";
    public static final int RC_DB_SYNC = 100;
    private final String TUTORIAL_SHOWN = "tutorialShown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkDBVersion();
        getActionBar().setTitle(R.string.mainMenu);
        setTypefaces();
    }

    private void showTutorial() {
        SharedPreferences sharedPreferences = getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(TUTORIAL_SHOWN, false)) {
            ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
            co.showcaseId = ShowcaseView.ITEM_ACTION_ITEM;
            co.hideOnClickOutside = true;
            ActionItemTarget target = new ActionItemTarget(this, R.id.syncDB);
            ShowcaseView sv = ShowcaseView.insertShowcaseView(target, this, R.string.syncDB, R.string.syncDBSV, co);
            sv.show();
            sv.setOnShowcaseEventListener(this);
            sharedPreferences.edit().putBoolean(TUTORIAL_SHOWN, true).commit();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_DB_SYNC:
                if (resultCode == RESULT_OK) {
                    findViewById(R.id.dbNeedUpgrade).setVisibility(View.INVISIBLE);
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setTypefaces() {
        Typeface tf = Typeface.createFromAsset(getAssets(), TITLE_FONT);
        TextView textView = (TextView) findViewById(R.id.mainTextView);
        textView.setTypeface(tf);
        textView = (TextView) findViewById(R.id.dbNeedUpgrade);
        textView.setTypeface(tf);
        Button button = (Button) findViewById(R.id.searchGame);
        button.setTypeface(tf);
        button = (Button) findViewById(R.id.gameList);
        button.setTypeface(tf);
        button = (Button) findViewById(R.id.gameFavorites);
        button.setTypeface(tf);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        showTutorial();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.syncDB:
                syncDB();
                return true;
            case R.id.properties:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickSearchGame(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    private void syncDB() {
        new Changelog(this).init();
    }

    public void onClickGameList(View view) {
        Intent intent = new Intent(this, GameListActivity.class);
        startActivity(intent);
    }

    public void onClickFavorites(View view) {
        Intent intent = new Intent(this, FavoritesActivity.class);
        startActivity(intent);
    }

    private void checkDBVersion() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(DBSyncActivity.versionURL);
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
                        sb.append(line);
                    }
                    is.close();
                    String version = sb.toString();
                    String localVersion = getSharedPreferences(SETTINGS_FILE, MODE_PRIVATE).getString(DB_VERSION_DATE, "usahdaui");
                    final int visible = !localVersion.trim().equals(version.trim()) ? View.VISIBLE : View.INVISIBLE;
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.dbNeedUpgrade).setVisibility(visible);
                            findViewById(R.id.updateProgressBar).setVisibility(View.INVISIBLE);
                        }
                    });
                } catch (Exception e) {
                    Log.e("log_tag", "Error converting result " + e.toString(), e);
                }
            }
        });
        t.start();
    }

    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {
    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
        // ShowcaseView was closed
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.syncNowTitle);
        builder.setMessage(R.string.syncNowMsg);

        // Set up the buttons
        builder.setPositiveButton(R.string.synchronize, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, DBSyncActivity.class);
                startActivityForResult(intent, MainActivity.RC_DB_SYNC);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {
    }
}
