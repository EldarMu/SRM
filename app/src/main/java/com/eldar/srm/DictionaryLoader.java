package com.eldar.srm;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by eldar on 4/15/2017.
 */
public class DictionaryLoader
{
    public DictionaryLoader() {}
    public void createLocalDict(String address)
    {
        URL url = null;
        try {
            url = new URL(address);
        } catch (MalformedURLException e) {
            Log.e("DOWNLOAD TASK",e.toString());
            return;
        }
        new DownloadFilesTask().execute(url);


    }
    private class DownloadFilesTask extends AsyncTask<URL,Void,String>
    {
        protected String doInBackground(URL... urls)
        {
            BufferedReader in = null;
            URL url = urls[0];
            try
            {
                in = new BufferedReader(new InputStreamReader(url.openStream()));
                String str;
                Log.d("DOWNLOAD TASK", "TESTING ITERATION");
                int iter = 0;
                while ((str = in.readLine()) != null)
                {
                    iter++;
                    Log.d("DOWNLOAD TASK", str);
                }
                Log.d("DOWNLOAD TASK", "iterated for " + iter + " rounds");
            }
            catch (IOException e)
            {
                Log.d("DOWNLOAD TASK", e.toString());
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch(IOException e)
                {
                    Log.e("DOWNLOAD TASK", e.toString());
                }
            }
            return url.toString();
        }

        protected void onPostExecute(String str) {
            Log.d("DOWNLOAD TASK", "dictionary downloaded from " + str);
        }
    }
}