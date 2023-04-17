import java.util.*;

import java.io.*;
/**
 * By using the methods of the LogicTree class, this provides the main method for parsing the input file, performing the CNF conversion algorithm
 * And then outputting the CNF into a new file
 *
 * @author Luke Jennings
 * @version Spring 2023
 */
public class CNFConverter
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
        
        //now that we have our input file, we must now scan the input line and then execute the CNFconversion alogirthm
        
        String inputPL = inFile.nextLine();
        
        //from here we construct our tree
        LogicTree lt = new LogicTree(null,inputPL);
        System.out.println(lt);
        
        //first step we perform is that we must remove all implications
        lt.removeAllImplications();
        System.out.println(lt);
        
        //next move all negations inward
        lt.removeNegations();
        System.out.println(lt);
        
        //third perform distribution so that all ands are near the root of the tree and all ors are to the bottom
        //this will allow us to extract out the CNF
        lt.applyDistribution();
        System.out.println(lt);
        
        //fourth we must get the arraylist of clauses, which themselves are arraylists of strings
        ArrayList<ArrayList<String>> clauses = lt.naiveCNF();
        
        //here we remove all tautogologies, anything that must be for certain true, as it ultimately means nothing of importance
        removeTautologies(clauses);
        
        
        for(ArrayList<String> clause: clauses){
            Collections.sort(clause);
            for(String var: clause){
                
                System.out.print(var + " ");
            }
            System.out.println();
        }
        
        try {
            // attach a file to FileWriter
            int random = (int) (100 * Math.random());
            File file = new File("output" + filename + "" + random + ".txt");
            PrintWriter output = new PrintWriter(file);
            ArrayList<String> outputStrings = new ArrayList<String>();
            StringBuffer sb;

            // read each clause, write it to file as comma seperated list
            //if contradiction simply write contradiction

            
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
    
    
    
}

    