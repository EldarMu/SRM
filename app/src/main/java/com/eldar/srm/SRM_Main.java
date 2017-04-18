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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class SRM_Main extends AppCompatActivity
{
    private static final int INTERNET_REQUEST_RESULT = 1;
    private static final int WAKE_LOCK_REQUEST_RESULT = 2;
    private List<String> returnableResult;
    private ProgressDialog downloadProgress;
    private static final String DOWNLOAD_TASK = "DOWNLOAD TASK";


    //This section is for setting up menus, turning on UI
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        returnableResult = null;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_srm__main);

        if (!checkPermissions())
        {
            getPermissions();
        }

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(onClickListener);

        downloadProgress = new ProgressDialog(SRM_Main.this);
        downloadProgress.setMessage("Downloading Dictionary");
        downloadProgress.setIndeterminate(true);
        downloadProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        downloadProgress.setCancelable(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.defaultmenu, menu);
        return true;
    }

    //This section is for Dealing with app permissions
    //Currently only need internet access to dowload new dictionaries

    private boolean checkPermissions()
    {
        boolean allPermissionsGranted = true;
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED)
        {
            allPermissionsGranted = false;
        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WAKE_LOCK)!= PackageManager.PERMISSION_GRANTED)
        {
            allPermissionsGranted = false;
        }
        return allPermissionsGranted;
    }
    private void getPermissions()
    {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},INTERNET_REQUEST_RESULT);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WAKE_LOCK},WAKE_LOCK_REQUEST_RESULT);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case INTERNET_REQUEST_RESULT:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){}
                else
                {
                    this.finishAffinity();
                }
                return;
            }
            case WAKE_LOCK_REQUEST_RESULT:
            {
                if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED){}
                else
                {
                    this.finishAffinity();
                }
                return;
            }
        }
    }

    //this section is for the listeners for various buttons, and their behavior
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.button:
                {
                    URL url = null;
                    try {
                        url = new URL("https://sites.google.com/site/neocennuznyjsajt/fajly/sample_dict.txt");
                    } catch (MalformedURLException e) {
                        Log.e(DOWNLOAD_TASK,e.toString());
                    }
                    final DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
                    downloadFilesTask.execute(url);

                    downloadProgress.setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialogInterface)
                        {
                            downloadFilesTask.cancel(true);
                        }
                    });

                    break;
                }
                case R.id.button2:
                    break;
                case R.id.button3:
                    break;
            }

        }
    };

    //this section is for any methods relating to button clicks that need their own class/methods

    //activated when merge button is selected and a valid URL is provided.
    //can't be kept in a separate class due to need to access UI elements (namely, a progres dialog)
    private class DownloadFilesTask extends AsyncTask<URL,Void,List<String>>
    {
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWakeLock.acquire();
            downloadProgress.show();
        }

        protected List<String> doInBackground(URL... urls)
        {
            BufferedReader in = null;
            URL url = urls[0];
            List<String> results = new LinkedList<String>();
            try
            {
                in = new BufferedReader(new InputStreamReader(url.openStream()));
                String str;
                int debugIterCount = 0;
                while ((str = in.readLine()) != null)
                {
                    debugIterCount++;
                    results.add(str);
                    Log.d(DOWNLOAD_TASK, str);
                }
                Log.d(DOWNLOAD_TASK, "iterated for " + debugIterCount + " rounds");
            }
            catch (IOException e)
            {
                Log.d(DOWNLOAD_TASK, e.toString());
                return null;
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch(NullPointerException e)
                {
                    Log.e(DOWNLOAD_TASK, e.toString());
                }
                catch(IOException e)
                {
                    Log.e(DOWNLOAD_TASK, e.toString());
                }
            }
            return results;
        }

        protected void onPostExecute(List<String> results)
        {
            mWakeLock.release();
            downloadProgress.dismiss();
            Log.d(DOWNLOAD_TASK, "dictionary downloaded");
            returnableResult = results;
        }
    }

}
