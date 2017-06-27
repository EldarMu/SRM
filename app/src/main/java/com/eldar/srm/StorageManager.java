package com.eldar.srm;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Created by eldar on 5/8/2017.
 * This class is responsible specifically for writing a single dictionary to
 * internal storage, and loading it from there should it already exist
 */
class StorageManager
{
    private final static String loadTag = "TASK: LOADING ";
    private final static String saveTag = "TASK: SAVING ";
    private final static String deleteTag = "TASK: DELETING "    ;
    private final static String baseFileName = "storedIntlDict.txt";

    private static String fileName(Context context)
    {
        return context.getFilesDir() + "/" + baseFileName;
    }


    static Dictionary load(Context context)
    {
        Log.e(loadTag, "Loading " + fileName(context));
        BufferedReader rdr = null;
        Dictionary dict = new Dictionary();
        try
        {
            rdr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName(context)), "utf8"), 8192);
            dict.read(rdr);
        }
        catch(UnsupportedCharsetException e)
        {
            Log.e(loadTag, "Charset string is incorrect or not recognized: " + e.toString());
        }
        catch(IOException e)
        {
            Log.e(loadTag, e.toString());
        }
        finally
        {
            try
            {
                if (rdr != null) {
                    rdr.close();
                }
            }
            catch(IOException e)
            {
                Log.e(loadTag, "Tried to close a null file input reader: " + e.toString());
            }
        }
        return dict;
    }

    static boolean save(Dictionary dict, Context context)
    {
        Log.e(saveTag, "Saving " + fileName(context));
        boolean isSuccessful = false;

        BufferedWriter wrt = null;
        try
        {
            wrt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName(context)), "utf8"),8192);
            dict.write(wrt);
            isSuccessful = true;
        }
        catch (FileNotFoundException e)
        {
            Log.e(saveTag, "System refused to make a new file for us to use: " + e.toString());
        }
        catch (IOException e)
        {
            Log.e(saveTag, "System refused to save: " + e.toString());
        }
        finally
        {
            try
            {
                if (wrt != null) {
                    wrt.close();
                }
            }
            catch (IOException e)
            {
                Log.e(saveTag, "Tried to close a null outputstream.");
            }
        }
        Log.e(saveTag, "Save outcome: " + isSuccessful);
        return isSuccessful;
    }

    static void deleteLocalCopy(Context context)
    {
        Log.e(deleteTag, "Deleting " + fileName(context));
        if (!context.deleteFile(baseFileName))
        {
            Log.e(deleteTag, "failed to locate the local copy for deletion");
        }
    }
}
