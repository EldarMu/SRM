package com.eldar.srm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by eldar on 4/26/2017.
 */
//this class creates bundles of dictionary entries with appropriate frequencies from each list
//and manages the movement of entries between lists during testing

public class SRMSession
{
    ArrayList<DictEntry> session;
    ArrayList<ArrayList<DictEntry>> dictionary;
    DictEntry currentEntry;
    int numOfLists;
    public SRMSession(ArrayList<ArrayList<DictEntry>> theDict)
    {
        session=null;
        currentEntry=null;
        dictionary = theDict;
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
        dictionary.get(score).add(tested);
    }

    private void createSession()
    {
        session = new ArrayList<DictEntry>();
        Random randGen = new Random();
        int multiplier = dictionary.size()-1;
        for (int i = 1; i < dictionary.size(); i++)
        {
            for (int j = 0; j < multiplier; j++)
            {
                int rand = randGen.nextInt(dictionary.get(i).size());
                session.add(dictionary.get(i).get(rand));
                //we remove the entries from the dictionary, as their priority values may change
                dictionary.get(i).remove(rand);
            }
            //so we pull five entries from the first, four from the second...one from the fifth
            multiplier--;
        }
        //and then we shuffle the session so it's not in order
        Collections.shuffle(session);
    }
}
