package de.memorian.ps4trophaen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A dialog the user can enter a message to comment a trophy.
 *
 * @author Tom-Philipp Seifert
 * @since 06.11.2014
 */
public class ReportErrorDialog {

    /**
     * URL to report error_ic PHP script.
     */
    private final String url = "http://www.seifertion.de/PS4Server/reporterror.php";
    private final Activity activity;

    public ReportErrorDialog(Activity activity) {
        this.activity = activity;
    }

    public void showReportDialogue(final String gameName, final String trophyName) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.reportTitle);
        String message = activity.getResources().getString(R.string.reportMessage, trophyName);
        builder.setMessage(message);
        View view = activity.getLayoutInflater().inflate(R.layout.report_error_dialogue, null);
        final EditText editText = (EditText) view.findViewById(R.id.reportEditText);
        editText.setHint(R.string.reportHint);
        builder.setView(view);
        // Set up the buttons
        builder.setPositiveButton(R.string.reportSend, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String message = editText.getText().toString();
                if (!message.isEmpty()) {
                    sendReport(gameName, trophyName, message);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });
    }

    private void sendReport(String gameName, String trophyName, String message) {
        new SendReportTask(gameName, trophyName, message).execute((Void) null);
    }

    private class SendReportTask extends AsyncTask<Void, Void, Boolean> {

        private final String gameName;
        private final String trophyName;
        private final String message;

        public SendReportTask(String gameName, String trophyName, String message) {
            this.gameName = gameName;
            this.trophyName = trophyName;
            this.message = message;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("name", gameName));
                nameValuePairs.add(new BasicNameValuePair("trophyName", trophyName));
                nameValuePairs.add(new BasicNameValuePair("info", message));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                httpclient.execute(httppost);
                return true;
            } catch (ClientProtocolException e) {
                Log.e(ReportErrorDialog.class.getName(), e.getMessage());
            } catch (IOException e) {
                Log.e(ReportErrorDialog.class.getName(), e.getMessage());
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(activity, R.string.reportSentSuccess, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, R.string.reportSentFailed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
