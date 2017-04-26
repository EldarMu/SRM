package com.eldar.srm;

/**
 * Created by eldar on 4/26/2017.
 */
public class DictEntry
{
    int priority;
    String wordCategory;
    String comment;
    String[] translations;

    public DictEntry(String line, int numOfLangs, int pri)
    {
        if (line == null || line.length() == 0) return;
        priority = pri;
        String[] words = line.split("/t");
        wordCategory = words[0];
        translations = new String[numOfLangs];
        for (int i = 1; i <= numOfLangs; i++)
        {
            translations[i-1] = words[i];
        }
        if (words.length > numOfLangs+1)
        {
            comment = words[words.length-1];
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {return true;}
        if(!(obj instanceof DictEntry)) {return false;}
        DictEntry theObject = (DictEntry)obj;
        return translations[0].equals(theObject.translations[0]);
    }

    @Override
    public int hashCode()
    {
        return translations[0].hashCode();
    }
}
