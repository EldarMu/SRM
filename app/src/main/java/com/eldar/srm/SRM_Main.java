package com.eldar.srm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class SRM_Main extends AppCompatActivity {
    private static final int INTERNET_REQUEST_RESULT = 1;
    private static final int WAKE_LOCK_REQUEST_RESULT = 2;
    private ProgressDialog downloadProgress;
    private static final String DOWNLOAD_TASK = "DOWNLOAD TASK";

    private Dictionary dict = new Dictionary();
    private String mergeDictURL = "https://sites.google.com/site/neocennuznyjsajt/fajly/dict.tsv";
    // private String mergeDictURL = "https://sites.google.com/site/neocennuznyjsajt/fajly/number.txt";


    
    //This section is for setting up menus, turning on UI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_srm__main);

        if (!checkPermissions()) {
            getPermissions();
        }

        RelativeLayout mrl = (RelativeLayout) findViewById(R.id.mainRelativeLayout);

        mrl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent arg1) {
                switchScreen(false);
                return true;  // Always return true to consume event.
            }
        });

        Button button = (Button) findViewById(R.id.buttonFail);
        button.setOnClickListener(onClickListener);
        button = (Button) findViewById(R.id.buttonGood);
        button.setOnClickListener(onClickListener);
        button = (Button) findViewById(R.id.buttonKeep);
        button.setOnClickListener(onClickListener);

        initializeDictionary();

        downloadProgress = new ProgressDialog(SRM_Main.this);
        downloadProgress.setMessage("Downloading Dictionary");
        downloadProgress.setIndeterminate(true);
        downloadProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        downloadProgress.setCancelable(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.defaultmenu, menu);
        return true;
    }

    //Initializing the dictionary element and Session
    private void initializeDictionary() {
        dict = StorageManager.load(getApplicationContext());
        updateTest(dict.next());
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
                new AlertDialog.Builder(this)
                        .setTitle("Delete local dictionary")
                        .setMessage("Are you sure you want to delete the local dictionary?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                StorageManager sm = new StorageManager();
                                sm.deleteLocalCopy(getApplicationContext());
                                initializeDictionary();

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
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
                return super.onOptionsItemSelected(item);

        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.buttonFail:
                    dict.front();
                    break;
                case R.id.buttonKeep:
                    dict.keep();
                    break;
                case R.id.buttonGood:
                    dict.back();
                    break;
                // Exhaustive list, no default.
            }
            switchScreen(true);
            updateTest(dict.next());
        }
    };

    @Override
    public void onPause() {
        StorageManager.save(dict, this);
        super.onPause();
    }

    @Override
    public void onStop() {
        StorageManager.save(dict, this);
        super.onStop();
    }

    @Override
    public void onResume() {
        initializeDictionary();
        super.onResume();
    }


    // This section is for any methods relating to button clicks that need their own class/methods.

    private void updateTest(Entry de) {
        TextView translations = (TextView) findViewById(R.id.textViewTranslations);
        TextView category = (TextView) findViewById(R.id.textViewCategory);
        TextView comments = (TextView) findViewById(R.id.textViewComments);
        TextView mainLanguage = (TextView) findViewById(R.id.textViewPrimaryLanguage);

        switchScreen(true);
        category.setText(de.getCategory());
        mainLanguage.setText(de.getWord());

        translations.setText(de.getTranslation("\n"));
        comments.setText(de.getComment());
    }

    private void switchScreen(boolean isInTestState) {
        TextView translations = (TextView) findViewById(R.id.textViewTranslations);
        TextView category = (TextView) findViewById(R.id.textViewCategory);
        TextView comments = (TextView) findViewById(R.id.textViewComments);
        Button goodButton = (Button) findViewById(R.id.buttonGood);
        Button keepButton = (Button) findViewById(R.id.buttonKeep);
        Button failButton = (Button) findViewById(R.id.buttonFail);
        RelativeLayout mrl = (RelativeLayout) findViewById(R.id.mainRelativeLayout);

        if (isInTestState) {
            translations.setVisibility(View.INVISIBLE);
            category.setVisibility(View.INVISIBLE);
            comments.setVisibility(View.INVISIBLE);
            goodButton.setEnabled(false);
            goodButton.setVisibility(View.INVISIBLE);
            keepButton.setEnabled(false);
            keepButton.setVisibility(View.INVISIBLE);
            failButton.setEnabled(false);
            failButton.setVisibility(View.INVISIBLE);
            mrl.setClickable(true);
        } else {
            translations.setVisibility(View.VISIBLE);
            category.setVisibility(View.VISIBLE);
            comments.setVisibility(View.VISIBLE);
            goodButton.setEnabled(true);
            goodButton.setVisibility(View.VISIBLE);
            keepButton.setEnabled(true);
            keepButton.setVisibility(View.VISIBLE);
            failButton.setEnabled(true);
            failButton.setVisibility(View.VISIBLE);
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

    //activated when merge button is selected and a valid URL is provided.
    //can't be kept in a separate class due to need to access UI elements (namely, a progress dialog)
    private class DownloadFilesTask extends AsyncTask<URL, Void, Dictionary> {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWakeLock.acquire();
            downloadProgress.show();
        }

        protected Dictionary doInBackground(URL... urls) {
            BufferedReader in = null;
            URL url = urls[0];
            Dictionary newDictionary = new Dictionary();
            //List<String> results = new ArrayList<String>();
            try {
                in = new BufferedReader(new InputStreamReader(url.openStream()));
                newDictionary = new Dictionary(in);
            } catch (IOException e) {
                Log.e(DOWNLOAD_TASK, e.toString());
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
            return newDictionary;
        }

        protected void onPostExecute(Dictionary results) {
            mWakeLock.release();
            downloadProgress.dismiss();
            Log.d(DOWNLOAD_TASK, "dictionary downloaded");
            dict.merge(results);
            StorageManager sm = new StorageManager();
            sm.save(dict, getApplicationContext());
            updateTest(dict.next());
        }
    }
}
