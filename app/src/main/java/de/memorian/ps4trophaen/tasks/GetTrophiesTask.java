package de.memorian.ps4trophaen.tasks;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import de.memorian.ps4trophaen.models.Trophy;
import de.memorian.ps4trophaen.storage.TrophyDBHelper;
import de.memorian.ps4trophaen.storage.TrophyDatabase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Receives the given MySQL table and puts the result in the SQLite database.
 *
 * @author Tom-Philipp Seifert
 * @since 11.10.2014
 */
public class GetTrophiesTask extends AsyncTask<Void, Void, Void> {

    private final String url = "https://seifertion.de/PS4Server/getgame.php";
    private TaskFinishedListener taskFinishedListener;
    private final TrophyDBHelper trophyDBHelper;
    private final String xref;

    public GetTrophiesTask(TrophyDBHelper trophyDBHelper, String xref) {
        this.trophyDBHelper = trophyDBHelper;
        this.xref = xref;
    }

    public void addListener(TaskFinishedListener listener) {
        taskFinishedListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        InputStream is = null;

        // Download JSON data from URL
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("dbname", xref));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();

        } catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString(), e);
        }

        // Convert response to string
        try {
            JsonReader jsonReader = new JsonReader(new InputStreamReader(is));
            jsonReader.beginArray();
            List<ContentValues> trophyValues = new ArrayList<ContentValues>();
            while (jsonReader.hasNext()) {
                try {
                    ContentValues values = readTrophy(jsonReader);
                    trophyValues.add(values);
                }catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Error reading a trophy", e);
                }
            }
            trophyDBHelper.addTrophies(xref, trophyValues);

            jsonReader.close();
        } catch (Exception e) {
            Log.e("log_tag", "Error converting result " + e.toString(), e);
        }

        return (Void) null;
    }

    protected void onPostExecute(Void params) {
        if (taskFinishedListener != null) {
            taskFinishedListener.onTaskFinished(this);
        }
    }

    private ContentValues readTrophy(JsonReader jsonReader) throws IOException {
        ContentValues values = new ContentValues();
        jsonReader.beginObject();
        String name;
        while (jsonReader.hasNext()) {
            name = jsonReader.nextName();
            if (TrophyDatabase.SECRET.equals(name)) {
                // secret value is contained in the type
                jsonReader.skipValue();
            } else if (TrophyDatabase.TYPE.equals(name)) {
                String val = jsonReader.nextString();
                Trophy.Type type = parseTrophyType(val, values);
                if (type == null) {
                    type = Trophy.Type.BRONZE;
                }
                values.put(TrophyDatabase.TYPE, type.name());
            } else if (TrophyDatabase.TITLE.equals(name)
                    || TrophyDatabase.TEXT.equals(name)
                    || TrophyDatabase.GUIDE.equals(name)
                    || TrophyDatabase.ICON.equals(name)) {
                values.put(name, jsonReader.nextString());
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return values;
    }

    /**
     * Parses the database value ("b", "s" etc) to actual types (BRONZE, SILVER etc).
     * Also sets if the trophy is secret.
     *
     * @param type   The database type string.
     * @param values The ContentValues that will be put into the db.
     * @return The parsed type or null.
     */
    private Trophy.Type parseTrophyType(String type, ContentValues values) {
        String[] split = type.split("\\|");
        // split adds a leading empty string, so just iterate over the array
        for (String s : split) {
            if ("sec".equalsIgnoreCase(s)) {
                values.put(TrophyDatabase.SECRET, true);
            }
        }
        for (String s : split) {
            if ("b".equalsIgnoreCase(s)) {
                return Trophy.Type.BRONZE;
            }
            if ("s".equalsIgnoreCase(s)) {
                return Trophy.Type.SILVER;
            }
            if ("g".equalsIgnoreCase(s)) {
                return Trophy.Type.GOLD;
            }
            if ("p".equalsIgnoreCase(s)) {
                return Trophy.Type.PLATIN;
            }
        }
        return null;
    }
}
