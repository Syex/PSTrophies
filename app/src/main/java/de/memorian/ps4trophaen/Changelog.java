package de.memorian.ps4trophaen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows a dialog that shows the last added games.
 *
 *
 * @since 04.11.2014
 */
public class Changelog {

    /**
     * URL to changelog PHP script.
     */
    private final String url = "";
    /**
     * Calling Activity.
     */
    private Activity mActivity;
    /**
     * XML received from the PHP script.
     */
    private String xmlData;
    /**
     * List containing the parsed changelog entries.
     */
    private List<ChangeEntry> changeEntryList = new ArrayList<ChangeEntry>();

    public Changelog(Activity context) {
        this.mActivity = context;
    }

    public void init() {
        getChanglog();
    }

    private void getChanglog() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream is = null;
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
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    xmlData = sb.toString();
                    buildChangeEntries();
                    showChangelog();
                } catch (Exception e) {
                    Log.e("log_tag", "Error converting result " + e.toString(), e);
                }
            }
        });
        t.start();
    }

    private void showChangelog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.changelogTitle);
        ScrollView scrollView = new ScrollView(mActivity);
        LinearLayout linearLayout = new LinearLayout(mActivity);
        scrollView.addView(linearLayout);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        for(ChangeEntry changeEntry : changeEntryList) {
            View changeEntryView = mActivity.getLayoutInflater().inflate(R.layout.activity_changelog, null);
            TextView textView = (TextView) changeEntryView.findViewById(R.id.changelogDate);
            textView.setText(changeEntry.getDate());

            textView = (TextView) changeEntryView.findViewById(R.id.changelogText);
            for(String text : changeEntry.getParagraphs()) {
                textView.setText(textView.getText() + "\u2022" + " " + text + "\n");
            }
            linearLayout.addView(changeEntryView);
        }

        builder.setView(scrollView);

        // Set up the buttons
        builder.setPositiveButton(R.string.synchronize, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(mActivity, DBSyncActivity.class);
                mActivity.startActivityForResult(intent, MainActivity.RC_DB_SYNC);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });
    }

    private void buildChangeEntries() {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(xmlData));
            int eventType = parser.getEventType();
            ChangeEntry changeEntry = new ChangeEntry();
            String text = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase("change")) {
                            changeEntry = new ChangeEntry();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (tagname.equalsIgnoreCase("change")) {
                            // add employee object to list
                            changeEntryList.add(changeEntry);
                        } else if (tagname.equalsIgnoreCase("date")) {
                            changeEntry.setDate(text);
                        } else if (tagname.equalsIgnoreCase("text")) {
                            changeEntry.addText(text);
                        }
                        break;

                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            Log.e(getClass().getName(), "Error converting XML", e);
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error converting XML", e);
        }
    }

    private class ChangeEntry {
        private String date;
        private List<String> paragraphs = new ArrayList<String>();

        private ChangeEntry() {
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void addText(String text) {
            paragraphs.add(text);
        }

        public String getDate() {
            return date;
        }

        public List<String> getParagraphs() {
            return paragraphs;
        }
    }

}
