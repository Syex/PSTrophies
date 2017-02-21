package de.memorian.ps4trophaen.tasks;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.memorian.ps4trophaen.DBSyncActivity;
import de.memorian.ps4trophaen.storage.GameOverviewDBHelper;
import de.memorian.ps4trophaen.storage.GameOverviewDatabase;
import de.memorian.ps4trophaen.storage.TrophyDBHelper;

/**
 * Receives the MySQL table "0games" and puts the result in the SQLite database.
 * Also it automatically invokes the GetTrophiesTask.
 *
 * @author Tom-Philipp Seifert
 * @since 10.10.2014
 */
public class GetGameOverviewTask extends AsyncTask<Void, Void, List<String>> {

    private final String url = "http://www.seifertion.de/PS4Server/getgames.php";
    private final DBSyncActivity dbSyncActivity;
    private final GameOverviewDBHelper gameOverviewDBHelper;
    private final TrophyDBHelper trophyDBHelper;

    public GetGameOverviewTask(DBSyncActivity dbSyncActivity,
                               GameOverviewDBHelper gameOverviewDBHelper,
                               TrophyDBHelper trophyDBHelper) {
        this.dbSyncActivity = dbSyncActivity;
        this.gameOverviewDBHelper = gameOverviewDBHelper;
        this.trophyDBHelper = trophyDBHelper;
    }

    @Override
    protected List<String> doInBackground(Void... params) {
        InputStream is = null;

        // Download JSON data from URL
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();

        } catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString(), e);
        }

        // Convert response to string
        try {
            JsonReader jsonReader = new JsonReader(new InputStreamReader(is));
            List<String> xRefs = new ArrayList<String>();
            jsonReader.beginArray();
            int i = 0;
            while (i < 5) {
                try {
                    ContentValues values = readGame(jsonReader);
                    String xRef = values.getAsString(GameOverviewDatabase.XREF);
                    xRefs.add(xRef);
                    gameOverviewDBHelper.addGame(values);
                    i++;
                } catch (Exception e) {
                }
            }

            jsonReader.close();
            return xRefs;
        } catch (Exception e) {
            Log.e("log_tag", "Error converting inputstream " + e.toString(), e);
        }

        return new ArrayList<String>();
    }

    protected void onPostExecute(List<String> xRefs) {
        ProgressHandler progressHandler = new ProgressHandler(dbSyncActivity, xRefs.size());
        for (String xRef : xRefs) {
            GetTrophiesTask getTrophiesTask = new GetTrophiesTask(trophyDBHelper, xRef);
            progressHandler.addTask(getTrophiesTask);
            getTrophiesTask.execute((Void) null);
        }
    }

    private ContentValues readGame(JsonReader jsonReader) throws IOException {
        ContentValues values = new ContentValues();
        jsonReader.beginObject();
        String name;
        while (jsonReader.hasNext()) {
            name = jsonReader.nextName();
            if (GameOverviewDatabase.NAME.equals(name)
                    || GameOverviewDatabase.XREF.equals(name)
                    || GameOverviewDatabase.INFOS.equals(name)
                    || GameOverviewDatabase.GAME_LOGO.equals(name)) {
                values.put(name, jsonReader.nextString());
            } else if (GameOverviewDatabase.PLATIN.equals(name)
                    || GameOverviewDatabase.GOLD.equals(name)
                    || GameOverviewDatabase.SILVER.equals(name)
                    || GameOverviewDatabase.BRONZE.equals(name)
                    || GameOverviewDatabase.POINTS.equals(name)) {
                values.put(name, jsonReader.nextInt());
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return values;
    }
}
