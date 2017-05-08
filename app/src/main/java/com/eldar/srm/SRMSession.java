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
    DictEntry currentEntry;
    int numOfLists;
    public SRMSession(ArrayList<ArrayList<DictEntry>> theDict)
    {
        session=null;
        currentEntry=null;
        intlDictionary = theDict;
        numOfLists = theDict.size()-1;
    }

    public DictEntry getNext()
    {
        if (session == null || session.size() == 0)
        {
            createSession();
        }
        DictEntry next = session.get(0);
        session.remove(0);
        return next;
    }

    public void update(DictEntry tested, int score)
    {
        tested.priority = score;
        intlDictionary.get(score).add(tested);
    }

    private void createSession()
    {
        session = new ArrayList<DictEntry>();
        long timeInNano = System.nanoTime();
        Random randGen = new Random(timeInNano);
        int multiplier = intlDictionary.size()-1;
        for (int i = 1; i < intlDictionary.size(); i++)
        {
            for (int j = 0; j < multiplier; j++)
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
