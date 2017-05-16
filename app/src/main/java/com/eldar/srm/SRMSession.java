package com.eldar.srm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by eldar on 4/26/2017.
 */
//this class creates bundles of intlDictionary entries with appropriate frequencies from each list
//and manages the movement of entries between lists during testing

public class SRMSession
{
    ArrayList<DictEntry> session;
    ArrayList<ArrayList<DictEntry>> intlDictionary;
    private DictEntry currentEntry;
    int numOfLists;

    public SRMSession(ArrayList<ArrayList<DictEntry>> theDict)
    {
        session=null;
        currentEntry = null;
        intlDictionary = theDict;
        numOfLists = theDict.size()-1;
    }

    public DictEntry getNext()
    {
        if (intlDictionary.size() == 1)
        {
            return intlDictionary.get(0).get(0);
        }
        if (session == null || session.size() == 0)
        {
            createSession();
        }
        currentEntry = session.get(0);
        session.remove(0);
        return currentEntry;
    }

    public void update(int score)
    {
        currentEntry.priority = score;
        intlDictionary.get(score).add(currentEntry);
    }

    public ArrayList<ArrayList<DictEntry>> endSession()
    {
        if (currentEntry != null)
        {
            intlDictionary.get(currentEntry.priority).add(currentEntry);
        }
        if (session != null)
        {
            for (int i = 0; i < session.size(); i++)
            {
                intlDictionary.get(session.get(i).priority).add(session.get(i));
            }
        }
        session = null;
        currentEntry = null;
        return intlDictionary;
    }

    private void createSession()
    {
        session = new ArrayList<DictEntry>();
        long timeInNano = System.nanoTime();
        Random randGen = new Random(timeInNano);
        int multiplier = intlDictionary.size()-1;
        for (int i = 1; i < intlDictionary.size(); i++)
        {
            for (int j = 0; j < multiplier && j < intlDictionary.get(i).size(); j++)
            {
                int rand = randGen.nextInt(intlDictionary.get(i).size());
                session.add(intlDictionary.get(i).get(rand));
                //we remove the entries from the intlDictionary, as their priority values may change
                intlDictionary.get(i).remove(rand);
            }
            //so we pull five entries from the first, four from the second...one from the fifth
            multiplier--;
        }
        //and then we shuffle the session so it's not in order
        Collections.shuffle(session);
    }
}
