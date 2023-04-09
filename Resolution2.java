import java.util.*;

import java.io.*;

/**
 * Part 2 of Logic Project. CNF PL logic
 *
 * @author Luke Jennings
 * @version 
 */
public class Resolution2
{
    public static void main(String args[]){
        if (args.length != 1) {
            System.err.println("Filename required as first command-line parameter.");
            System.exit(1);
        }

        // Store the name of the file in this variable for readability.
        String filename = args[0];

        Scanner inFile = null;

        try {
            inFile = new Scanner(new File(filename));
        } catch (java.io.FileNotFoundException fnfe) {
            System.err.println(filename + " cannot be found in project folder.");
            System.exit(1);
        }

        //this array is the abstraction of our knowledge base
        //essentially since our knowldge base is all in cnf form, we are able to seperate each clause into it's own individual list of string arrays
        //and within each string array, the individual variables are stored at each index
        ArrayList<ArrayList<String>> clauses = new ArrayList<>();

        //new clauses is used to determine if our most recent loop through resolving each pair of clauses actually added new information to our knowledge base
        //if this new set of clauses is a subset of our original set of clauses from before the loop, then we actually haven't gained any information
        //hence that mean that we can end execution of the loop
        ArrayList<ArrayList<String>> newClauses = new ArrayList<>();

        //the resolvent is essentially the end result of resolving any two clauses with each other
        //if the resolvent is ever output as empty when resolving two different clauses, then we know a contradiction has occured, and we can stop the loop
        ArrayList<ArrayList<String>> resolvents = new ArrayList<>();

        int count = 0;
        //this is simply used to track what the current string being read in by the scanner is
        String curr;

        //once the input string is tokenized and seperated, we store it in this string array
        String[] currClause;
        ArrayList<String> currClauseArr;

        //these are used to strore clause_i and clause_j during the resolving process
        ArrayList<String> c1;
        ArrayList<String> c2;

    
        boolean contradiction = false;
        boolean contained;
        boolean subset = true;

        while(inFile.hasNextLine()){
            count++;
            //read in the next clause
            curr = inFile.nextLine();

            //replace all whitespace so we don't have to worry about it when seperating the string at the commas
            curr.replaceAll(" ","");

            //seperates string at commas, creating an array of just the variables in the clause
            currClause = curr.split(",");

            //just to standardize our output
            Arrays.sort(currClause);

            //adding the currentclause to our knowledge base
            currClauseArr = new ArrayList<String>();
            for(String s: currClause){
                currClauseArr.add(s);
            }
            clauses.add(currClauseArr);

            //System.out.println(count + ". " + Arrays.toString(currClause) + " (premise)");
        }

        //do while loop that determines whether or not we reached a contradiction, or if all the new clauses that have been generated are merely

        do{
            newClauses.clear();
            for(int i = 0; i < clauses.size(); i++){

                c1 = clauses.get(i);

                for(int j = i + 1; j < clauses.size(); j++){

              
                    c2 = clauses.get(j);
                   
                    resolvents.clear();

                    resolve(c1,c2,resolvents);
                    
                    //now we must check if the resolvents contains an empty clause
                    //because if it does then it is a sign that we have reached a contradiction
                    
                    for(ArrayList<String> r: resolvents){
                        
                        if(r.size() == 0){
                            contradiction = true;
                        }
                        Collections.sort(r);
                    }
                    
                    if(contradiction){
                        break;
                    }
                    
                    newClauses.addAll(resolvents);

                }
                if(contradiction) break;
                
                
            }
            
            //next remove all tautologies from the newClauses
            removeTautologies(newClauses);
            if(contradiction) break;
            
            if(clauses.containsAll(newClauses)) break;
            for(ArrayList<String> nc: newClauses){
                if(!clauses.contains(nc)){
                    clauses.add(nc);
                }
            }
            //clauses.addAll(newClauses);
                

        }while (!contradiction);

        try {
            // attach a file to FileWriter
            int random = (int) (100 * Math.random());
            File file = new File("output" + filename + "" + random + ".txt");
            PrintWriter output = new PrintWriter(file);
            ArrayList<String> outputStrings = new ArrayList<String>();
            StringBuffer sb;

            // read each clause, write it to file as comma seperated list
            //if contradiction simply write contradiction

            if(contradiction){
                System.out.println("Contradiction");
                output.printf("Contradiction\n");

            } else {
                for(ArrayList<String> c: clauses){
                    sb = new StringBuffer("");
                    //System.out.println(c.length);
                    for(int i = 0; i < c.size() - 1; i++){
                       
                        sb.append(c.get(i) + ",");
                       
                    }
                    sb.append(c.get(c.size() - 1));
                    //output.printf("%s\n",c[c.length - 1]);
                    outputStrings.add(sb.toString());
                }

                Collections.sort(outputStrings);

                for(String s: outputStrings){
                    output.printf("%s\n",s);
                    System.out.println(s);

                }
            }

            //close the file
            output.flush();
            output.close();

            System.out.println("Successfully written");

        }
        catch (Exception e) {
            e.getStackTrace();
        }

    }
    /*
     * This function is used to determine the resolvent of two clauses in CNF form
     * 
     * This is done by combining both clauses together, and then removing any variable in which its negation also exists in the resolvent
     * 
     * This algorithm is heavily inspired by the algorithm for PL resolution from 
     * https://github.com/aimacode/aima-java/blob/f3366884fd32c79fce1f3d2948344062df853e23/aima-core/src/main/java/aima/core/logic/propositional/inference/PLResolution.java#L121
     * 
     * However, I do not have access to the same data structures that the code above has, as such I will primarily use string arraylists
     * 
     * @param clause_i A string array of variables all disjunctive from each other
     * @param clause_j A string array of variables all disjunctive from each other
     * @return arraylist of resolvent clauses as a result of clause_i and clause_j to be added to our knowledge base
     */
    public static void resolve(ArrayList<String> clause_i, ArrayList<String> clause_j, ArrayList<ArrayList<String>> resolvents){

        

        resolvents.addAll(resolvePositiveWithNegative(clause_i,clause_j));
        resolvents.addAll(resolvePositiveWithNegative(clause_j,clause_i));
    }

    public static ArrayList<ArrayList<String>> resolvePositiveWithNegative(ArrayList<String> c1,ArrayList<String> c2){
        //first we must calculate which compliments exist between both clause 1 and clause 2, this is what will determine how many resolvents we have

        ArrayList<String> complements = intersection(getPositiveVariables(c1),getNegativeVariables(c2));

        ArrayList<String> resolventVariables;

        ArrayList<ArrayList<String>> resolvents = new ArrayList<ArrayList<String>>();

        //now for each set of compliments we must generate a new resolvent

        //for example say if clause 1 is a,-b,-c
        //and clause 2 is a,b,c
        //this means we need to make 2 resolvent clauses, first a,b,-b and a,c,-c

        for(String complement: complements){
            resolventVariables = new ArrayList<String>();
            //now we must add all variables from clause 1 that is not a part of this compliment
            //since we only resolved the positive variables from clause 1, we only need to check whether or not it is negative
            //or if it not in the complement

            for(String s: c1){
                if(s.charAt(0) == '~' || !s.equals(complement)){
                    if(!resolventVariables.contains(s)) resolventVariables.add(s);
                }
            }

            //now we must add all variable from clause 2 that i snot part of this complement
            //since we only resolved the negative variables from clause 2, we only need to check whether or not it is positive
            //or if it not in the complement
            for(String s: c2){
                if(s.charAt(0) != '~' || !s.substring(1).equals(complement)){
                    if(!resolventVariables.contains(s)) resolventVariables.add(s);
                }
            }

            //finally, if our clause contains a tautology (for example A and ~A, then we didn't actually learn anything, so we are able to remove it)
            resolvents.add(resolventVariables);
        }

        return resolvents;
    }

    /*
     * This function will return the intersection of any two String ArrayLists.
     *
     */
    public static ArrayList<String> intersection(ArrayList<String> arr1, ArrayList<String> arr2){
        if (arr1 == arr2) {
            return arr1;
        }
        Set<String> intersection = new LinkedHashSet<String>(arr1);
        intersection.retainAll(arr2);

        ArrayList<String> output = new ArrayList<>(intersection);

        //Collections.sort(output);

        return output;
        
    }

    /*
     * This will only return any variables that are not negated in a clause
     */
    public static ArrayList<String> getPositiveVariables(ArrayList<String> arr1){
        ArrayList<String> output = new ArrayList<>();

        for(String s: arr1){
            if(s.charAt(0) != '~'){
                output.add(s);
            }
        }

        return output;
    }

    /*
     * This will only return any variables that are negated in clause
     */
    public static ArrayList<String> getNegativeVariables(ArrayList<String> arr1){
        ArrayList<String> output = new ArrayList<>();

        for(String s: arr1){
            if(s.charAt(0) == '~'){
                output.add(s.substring(1));
            }
        
        }

        return output;
    }
    
    public static void removeTautologies(ArrayList<ArrayList<String>> clauses){
        for(int i = 0; i < clauses.size(); i++){
            if(isTautology(clauses.get(i))){
                clauses.remove(i);
                i--;
            }
        }
        
    }
    
    public static boolean isTautology(ArrayList<String> clause){
        
        
        if(intersection(getPositiveVariables(clause),getNegativeVariables(clause)).size() != 0){
            return true;
        }
        
        return false;
    }
}
