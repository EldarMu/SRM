package com.eldar.srm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SRM_Main extends AppCompatActivity {
    private static final int INTERNET_REQUEST_RESULT = 1;
    private static final int WAKE_LOCK_REQUEST_RESULT = 2;
    private ProgressDialog downloadProgress;
    private static final String DOWNLOAD_TASK = "DOWNLOAD TASK";

    private ArrayList<ArrayList<DictEntry>> theDictionary;
    private int numOfLists;
    private String mergeDictURL = "https://sites.google.com/site/neocennuznyjsajt/fajly/sample_dict.txt";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    //This section is for setting up menus, turning on UI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        numOfLists = 3;
        theDictionary = new ArrayList<ArrayList<DictEntry>>();

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_srm__main);

        if (!checkPermissions()) {
            getPermissions();
        }

        RelativeLayout mrl = (RelativeLayout) findViewById(R.id.mainRelativeLayout);

        mrl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent arg1)
            {
                switchScreen(false);
                return true;//always return true to consume event
            }
        });

        Button button = (Button) findViewById(R.id.buttonFail);
        button.setOnClickListener(onClickListener);
        button = (Button) findViewById(R.id.buttonGood);
        button.setOnClickListener(onClickListener);
        button = (Button) findViewById(R.id.buttonKeep);
        button.setOnClickListener(onClickListener);

        downloadProgress = new ProgressDialog(SRM_Main.this);
        downloadProgress.setMessage("Downloading Dictionary");
        downloadProgress.setIndeterminate(true);
        downloadProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        downloadProgress.setCancelable(true);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.defaultmenu, menu);
        return true;
    }

    //This section is for Dealing with app permissions
    //Currently only need internet access to download new dictionaries

    private boolean checkPermissions() {
        boolean allPermissionsGranted = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            allPermissionsGranted = false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            allPermissionsGranted = false;
        }
        return allPermissionsGranted;
    }

    private void getPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, INTERNET_REQUEST_RESULT);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WAKE_LOCK}, WAKE_LOCK_REQUEST_RESULT);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case INTERNET_REQUEST_RESULT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    this.finishAffinity();
                }
                return;
            }
            case WAKE_LOCK_REQUEST_RESULT: {
                if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    this.finishAffinity();
                }
                return;
            }
        }
    }

    //this section is for the listeners for various buttons, and their behavior

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:
                //Empties the Lists
                return true;

            case R.id.merge:
                final EditText urlText = new EditText(this);
                urlText.setText(mergeDictURL);
                urlText.setTextSize(10.0f);
                new AlertDialog.Builder(this)
                        .setTitle("Merge Dictionaries")
                        .setMessage("Please provide intlDictionary URL")
                        .setView(urlText)
                        .setPositiveButton("Merge Dictionary", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String url = urlText.getText().toString();
                                mergeDictionaries(url);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.buttonFail:
                {
                    switchScreen(true);
                    break;
                }
                case R.id.buttonKeep:
                {
                    switchScreen(true);
                    break;
                }
                case R.id.buttonGood:
                {
                    switchScreen(true);
                    break;
                }
            }

        }
    };

    //this section is for any methods relating to button clicks that need their own class/methods
    private void switchScreen(boolean isInTestState)
    {
        TextView translations = (TextView) findViewById(R.id.textViewTranslations);
        TextView category = (TextView) findViewById(R.id.textViewCategory);
        TextView comments = (TextView) findViewById(R.id.textViewComments);
        Button goodButton = (Button) findViewById(R.id.buttonGood);
        Button keepButton = (Button) findViewById(R.id.buttonKeep);
        Button failButton = (Button) findViewById(R.id.buttonFail);
        RelativeLayout mrl = (RelativeLayout) findViewById(R.id.mainRelativeLayout);

        if (isInTestState)
        {
            translations.setVisibility(View.INVISIBLE);
            category.setVisibility(View.INVISIBLE);
            comments.setVisibility(View.INVISIBLE);
            goodButton.setEnabled(false);
            keepButton.setEnabled(false);
            failButton.setEnabled(false);
            mrl.setClickable(true);
        }
        else
        {
            translations.setVisibility(View.VISIBLE);
            category.setVisibility(View.VISIBLE);
            comments.setVisibility(View.VISIBLE);
            goodButton.setEnabled(true);
            keepButton.setEnabled(true);
            failButton.setEnabled(true);
            mrl.setClickable(false);
        }
    }

    private void mergeDictionaries(String linkedURL) {
        URL url = null;
        try {
            url = new URL(linkedURL);

            final DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
            downloadFilesTask.execute(url);

            downloadProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    downloadFilesTask.cancel(true);
                }
            });

        } catch (MalformedURLException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Incorrect URL")
                    .setMessage("The text you typed in was not recognized as a URL")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .show();
            Log.e(DOWNLOAD_TASK, e.toString());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "SRM_Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.eldar.srm/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "SRM_Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.eldar.srm/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    //activated when merge button is selected and a valid URL is provided.
    //can't be kept in a separate class due to need to access UI elements (namely, a progres dialog)
    private class DownloadFilesTask extends AsyncTask<URL, Void, List<String>> {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWakeLock.acquire();
            downloadProgress.show();
        }

        protected List<String> doInBackground(URL... urls) {
            BufferedReader in = null;
            URL url = urls[0];
            List<String> results = new ArrayList<String>();
            try {
                in = new BufferedReader(new InputStreamReader(url.openStream()));
                String str;
                int debugIterCount = 0;
                while ((str = in.readLine()) != null) {
                    debugIterCount++;
                    results.add(str);
                    Log.d(DOWNLOAD_TASK, str);
                }
                Log.d(DOWNLOAD_TASK, "iterated for " + debugIterCount + " rounds");
            } catch (IOException e) {
                Log.d(DOWNLOAD_TASK, e.toString());
                return null;
            } finally {
                try {
                    in.close();
                } catch (NullPointerException e) {
                    Log.e(DOWNLOAD_TASK, e.toString());
                } catch (IOException e) {
                    Log.e(DOWNLOAD_TASK, e.toString());
                }
            }
            return results;
        }

        protected void onPostExecute(List<String> results) {
            mWakeLock.release();
            downloadProgress.dismiss();
            Log.d(DOWNLOAD_TASK, "intlDictionary downloaded");
            DictionaryBuilder dictBuild = new DictionaryBuilder();
            theDictionary = dictBuild.getMerged(theDictionary, results, numOfLists);
        }
    }

}
