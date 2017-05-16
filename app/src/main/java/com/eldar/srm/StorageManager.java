package com.eldar.srm;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;

/**
 * Created by eldar on 5/8/2017.
 * This class is responsible specifically for writing a single International Dictionary to
 * internal storage, and loading it from there should it already exist
 */
public class StorageManager
{
    public boolean save (ArrayList<ArrayList<DictEntry>> intlDictionary, Context context)
    {
        String saveTag = "TASK: SAVING ";
        boolean isSuccessful = false;
        String textFile = reformatDictAsString(intlDictionary);

        File file = new File(context.getFilesDir(), "storedIntlDict.txt");
        Writer wrt = null;
        try
        {
            wrt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf8"),8192);
            wrt.write(textFile);
            isSuccessful = true;
        }
        catch (FileNotFoundException e)
        {
            Log.e(saveTag, "system refused to make a new file for us to use " + e.toString());
        }
        catch (IOException e)
        {
            Log.e(saveTag, "system refused to save " + e.toString());
        }
        finally
        {
            try
            {
                wrt.close();
            }
            catch (IOException e)
            {
                Log.e(saveTag, "tried to close a null outputstream");
            }
        }
        return isSuccessful;
    }
    public void deleteLocalCopy(Context context)
    {
        if (!context.deleteFile("storedIntlDict.txt"))
        {
            //Log.d("TASK: DELETING ", "failed to locate the local copy for deletion");
        }
    }

    public boolean savedDictExists(Context context)
    {
        File file = context.getFileStreamPath("storedIntlDict.txt");
        return file.exists();
    }

    public ArrayList<ArrayList<DictEntry>> loadIntlDictionary(Context context)
    {
        String loadTag = "TASK: LOADING ";

        StringBuilder sb = new StringBuilder("");
        String path = context.getFilesDir() + "/storedIntlDict.txt";
        BufferedReader rdr = null;
        String line = "";
        try
        {
            rdr = new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf8"), 8192);
            while ((line = rdr.readLine())!= null)
            {
                sb.append(line+"\n");
            }
        }
        catch(UnsupportedCharsetException e)
        {
            Log.e(loadTag, "charset string is incorrect or not recognized" + e.toString());
        }
        catch(IOException e)
        {
            Log.e(loadTag, e.toString());
        }
        finally
        {
            try
            {
                rdr.close();
            }
            catch(IOException e)
            {
                Log.e(loadTag, "tried to close a null file input reader " + e.toString());
            }
        }
        return formatStringAsDict(sb.toString());
    }

    private ArrayList<ArrayList<DictEntry>> formatStringAsDict(String data)
    {
        //create the intlDictionary we're going to use
        ArrayList<ArrayList<DictEntry>> theDictionary = new ArrayList<ArrayList<DictEntry>>();


        String[] wordsToAdd = data.split("\n");

        //figure out number of languages used in intlDictionary
        int numOfLangs = 3;

        /*int maxPri = 0;
        //Right now default number of lists is 4, this can be modified in the future as a passed variable, but right now it's 4
        for (int i = 0; i < wordsToAdd.length; i++)
        {
            if (Character.getNumericValue(wordsToAdd[i].charAt(0)) > maxPri)
            {
                maxPri = Character.getNumericValue(wordsToAdd[i].charAt(0));
            }
        }*/

        //create the List that will contain the template entry (lists the names of the languages)
        ArrayList<DictEntry> template = new ArrayList<DictEntry>();
        //create the intlDictionary entry of the template
        DictEntry header = new DictEntry(wordsToAdd[0], numOfLangs, 0);
        template.add(header);
        theDictionary.add(template);

        for (int i = 0; i < 3; i++)
        {
            ArrayList<DictEntry> certainPriList = new ArrayList<DictEntry>();
            theDictionary.add(certainPriList);
        }
        //now we have all of the lists, list 0 is just for template, lists 1-N for our diff priorities

        for (int i = 1; i < wordsToAdd.length; i++)
        {
            DictEntry de = new DictEntry(wordsToAdd[i].substring(2), numOfLangs, Character.getNumericValue(wordsToAdd[i].charAt(0)));
            theDictionary.get(de.priority).add(de);
        }
        return theDictionary;
    }


    private String reformatDictAsString(ArrayList<ArrayList<DictEntry>> intlDictionary)
    {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < intlDictionary.size(); i++)
        {
            for (int j = 0; j < intlDictionary.get(i).size(); j++)
            {
                // so the result looks like LISTINDEX\tCATEGORY\tFIRSTLANGUAGE\tSECONDLANGUAGE\t...\tCOMMENT\n
                sb.append(String.valueOf(i) + "\t");
                sb.append(intlDictionary.get(i).get(j).wordCategory);
                for (int k = 0; k < intlDictionary.get(i).get(j).translations.length; k++)
                {
                    sb.append("\t" + intlDictionary.get(i).get(j).translations[k]);
                }
                if (intlDictionary.get(i).get(j).comment!=null)
                {
                    sb.append( "\t" + intlDictionary.get(i).get(j).comment + "\n");
                }
                else
                {
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }


}
