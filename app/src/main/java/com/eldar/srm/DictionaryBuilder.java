package com.eldar.srm;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eldar on 4/26/2017.
 */
public class DictionaryBuilder
{
    public ArrayList<ArrayList<DictEntry>> getMerged(ArrayList<ArrayList<DictEntry>> currentDict, List<String> wordsToAdd, int numbOfLists)
    {
        //create the dictionary we're going to use
        ArrayList<ArrayList<DictEntry>> theDictionary = null;

        switch (currentDict.size())
        {
            case 0:
            {
                //this shouldn't happen
                Log.e("TASK: MERGING", "a dictionary that had no entries at all was passed in. How?");
                break;
            }

            case 1:
            {
                //figure out number of languages used in dictionary
                int numOfLangs = wordsToAdd.get(0).split("/t").length-1;

                //since the only way size will be one is if it only contained the stub, we remove the stub
                currentDict.remove(0);

                //create the List that will contain the template entry (lists the names of the languages)
                ArrayList<DictEntry> template = new ArrayList<DictEntry>();
                //create the dictionary entry of the template
                DictEntry header = new DictEntry(wordsToAdd.get(0), numOfLangs, 0);
                template.add(header);
                theDictionary.add(template);

                //since we had only a stub, we're putting all of the entries into the first real list
                ArrayList<DictEntry> initialList = new ArrayList<DictEntry>();
                for (int i = 1; i < wordsToAdd.size(); i++)
                {
                    DictEntry dictEntry = new DictEntry(wordsToAdd.get(i), numOfLangs, 1);
                    initialList.add(dictEntry);
                }
                theDictionary.add(initialList);

                //still create the other lists, even though they initially contain nothing
                for (int j = 1; j < numbOfLists; j++)
                {
                    theDictionary.add(new ArrayList<DictEntry>());
                }
                //at this point, we've added all the lists that we're going to need.
                //the first list only holds the template
                //the rest of the lists separate the dictionary entries based on how frequently they ought to be brought up to the user

            }
            // if it's above 1, we're treating it as a legitimate list of lists that has some information already stored in it
            // we merge the new data with the existing
            // if a value already exists, we can change everything but the initial language word itself (which serves as a key)
            // if it does not, we add the word to the first list
            default:
            {
                theDictionary = currentDict;
                //figure out number of languages used in dictionary
                int numOfLangs = wordsToAdd.get(0).split("/t").length-1;
                //check if merged list has more words
                if (numOfLangs != theDictionary.get(0).get(0).translations.length)
                {
                    //!Current Behavior - language number mismatch will lead to unstable behavior!
                    //!Old dictionary removed, only new remains!

                    for (int i = 0; i < theDictionary.size(); i++)
                    {
                        theDictionary.remove(i);
                    }

                }

                ArrayList<DictEntry> tempArrList = new ArrayList<DictEntry>();
                //turn these words into mldes
                for (int i = 0; i < wordsToAdd.size(); i++)
                {
                    //the default list to add things to is 1
                    tempArrList.add(new DictEntry(wordsToAdd.get(i), numOfLangs, 1));
                }

                for (int i = 1; i < theDictionary.size(); i++)
                {
                    ArrayList<DictEntry> secondTemp = new ArrayList<DictEntry>();

                    for (int j = 1; j < tempArrList.size(); j++)
                    {

                        if (theDictionary.get(i).contains(tempArrList.get(j)))
                        {
                            tempArrList.get(j).priority = i;
                            //this works because our classes equals method compares specifically the first language of the two objects
                            theDictionary.get(i).remove(tempArrList.get(j));
                            theDictionary.get(i).add(tempArrList.get(j));
                        }
                        else
                        {
                            //add to second temp ArrayList, which we'll use to overwrite the one that had values we have already assigned to their
                            //final lists
                            secondTemp.add(tempArrList.get(j));

                        }
                    }
                    tempArrList = secondTemp;
                }

                //upon exiting, the only values that could remain in the temporary ArrayList are the ones that it did not contain before
                //we assign them to list 1, and they already have the appropriate priority
                for (int i = 0; i < tempArrList.size(); i++)
                {
                 theDictionary.get(1).add(tempArrList.get(i));
                }
            }

        }
        //so now we've assigned behavior to both size 1 (stub entry) and more than size 1 lists of lists
        //and we return it
        return theDictionary;
    }
}
