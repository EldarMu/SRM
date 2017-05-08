package com.eldar.srm;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
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
        FileOutputStream fileOutputStream = null;
        try
        {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(textFile.getBytes());
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
                fileOutputStream.close();
            }
            catch (IOException e)
            {
                Log.e(saveTag, "tried to close a null outputstream");
            }
        }
        return isSuccessful;
    }

    public boolean savedDictExists(Context context)
    {
        File file = context.getFileStreamPath("storedIntlDict.txt");
        return file.exists();
    }

    public ArrayList<ArrayList<DictEntry>> loadIntlDictionary(Context context)
    {
        String loadTag = "TASK: LOADING ";
        int ch;
        StringBuffer fileContent = new StringBuffer();
        FileInputStream fis = null;
        try
        {
            fis = context.openFileInput("storedIntlDict.txt");
            try {
                while( (ch = fis.read()) != -1)
                {
                    fileContent.append((char) ch);
                }
            }
            catch (IOException e)
            {
                Log.e(loadTag, "failed to parse file to text" + e.toString());
            }
        }
        catch (FileNotFoundException e)
        {
            Log.e(loadTag, "Failed to open file " + e.toString());
        }
        finally
        {
            try
            {
                fis.close();
            }
            catch(IOException e)
            {
                Log.e(loadTag, "tried to close a null file input reader " + e.toString());
            }
        }
        String data = new String(fileContent);
        return formatStringAsDict(data);
    }

    private ArrayList<ArrayList<DictEntry>> formatStringAsDict(String data)
    {
        //create the intlDictionary we're going to use
        ArrayList<ArrayList<DictEntry>> theDictionary = null;


        String[] wordsToAdd = data.split("\n");

        //figure out number of languages used in intlDictionary
        int numOfLangs = wordsToAdd[0].split("\t").length-1;

        int maxPri = 0;
        //figure out num of lists
        for (int i = 0; i < wordsToAdd.length; i++)
        {
            if (Character.getNumericValue(wordsToAdd[i].charAt(0)) > maxPri)
            {
                maxPri = Character.getNumericValue(wordsToAdd[i].charAt(0));
            }
        }

        //create the List that will contain the template entry (lists the names of the languages)
        ArrayList<DictEntry> template = new ArrayList<DictEntry>();
        //create the intlDictionary entry of the template
        DictEntry header = new DictEntry(wordsToAdd[0], numOfLangs, 0);
        template.add(header);
        theDictionary.add(template);

        for (int i = 0; i < maxPri; i++)
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
