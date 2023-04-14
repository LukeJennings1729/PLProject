import java.util.*;

import java.io.*;
/**
 * This is the tree data structure that I am using to store the propositional logic from my inputs
 * Contains the contructors, helpers, and CNFconversion functions, accidentally in functional style over object oriented (my bad)
 * @author Luke Jennings
 * @version Spring 2023
 */
public class LogicTree 
{
    //whether or not the underlying tree is negated or not
    boolean negated;
    //the operation being performed the two sub clauses
    //either | (or), & (and), > (implies), = (biconditional)
    char operator;

    //stores the possible subclauses involved with the operation
    //this should in most cases be 2, but when trying to fix biconditionals
    //this value may increase
    ArrayList<LogicTree> children;

    //just used to keep track of the pointer of the operation above, potientially important
    LogicTree parent;

    //this is simply used to keep track of what the input string is when constructing the input
    String input;

    //if variable is @ then that means that the current node is not a leaf node
    char variable;

    public static String operators = "&|>=";

    public LogicTree(LogicTree parent, String inputString){
        this.parent = parent;
        this.input = inputString;
        this.variable = ';';
        this.children = new ArrayList<LogicTree>();

        //first sanitize input by replacing all whitespace in the input string with empty strings, hence we don't have to care about whitespace in processing
        if(input.contains(" ")){
            input = input.replaceAll(" ", "");

        } 

        //if the current statement is negated then we create a node
        if(input.charAt(0) == '~') {
            this.negated = true;
            input = input.substring(1);
        } 

        if(input.charAt(0) == '('){
            int count = 1;

            for(int i = 1; i < input.length(); i++){
                if(input.charAt(i) == '('){
                    count++;

                } else if(input.charAt(i) == ')'){
                    count--;

                } else if(count == 1 & operators.contains("" + input.charAt(i))){
                    //if the count is equal to 1, and an operator appears
                    //this mean that this is the operator seperating the two clauses
                    this.operator = input.charAt(i);
                    //now we must determine using the length of the strings what constructor we must use for the children
                    //first we must remove the leftmost parenthesis and then find the previous closed parethesis
                    //if that is not possible, then we know we are dealing with the most basic variable, and then we create a leaf node
                    String left = input.substring(1,i);

                    if(left.length() == 1){
                        children.add(new LogicTree(this,left.charAt(0),false));
                    } else if(left.length() == 2){
                        children.add(new LogicTree(this,left.charAt(1),true));
                    } else {
                        children.add(new LogicTree(this,left));

                    }
                    String right = input.substring(i+1,input.length()-1);
                    if(right.length() == 1){
                        children.add(new LogicTree(this,right.charAt(0),false));

                    } else if (right.length() == 2){
                        children.add(new LogicTree(this,right.charAt(1),true));
                    } else {
                        children.add(new LogicTree(this,right));
                    }

                    break;
                }
            }
        }
    }

    /**
        This is the constructor for single char variable leaf nodes
        We will know for sure that
     */
    public LogicTree(LogicTree parent, char var, boolean negated){

        this.parent = parent;
        this.variable = var;
        this.negated = negated;
        this.operator = ';';

        this.children = null;
    }

    /**
        This is for creating operator nodes, but as a consequence of removing a biconditional
     */
    public LogicTree(LogicTree parent, LogicTree pNode, LogicTree qNode, char operator, boolean negated){
        this.children = new ArrayList<LogicTree>();

        children.add(pNode);
        children.add(qNode);
        this.operator = operator;
        this.negated = negated;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        if(this.operator != ';'){
            sb.append(operator + " ");
        } else {
            sb.append(variable + " ");
        }

        if(this.children != null){

            for(LogicTree c:children){
                sb.append(c.toString() + " ");
            }
        }
        
        return sb.toString();
        
    }

    /**
        This checks if the current node contains an implication operator
        Used later for removing implications
     */
    public static boolean isImplication(LogicTree lt){
        if(lt.operator == '>' && lt.children.size() == 2) return true;
        return false;
    }

    /**
        This checks if the current node contains a biconditional operator
        Used later for removing implications
     */
    public static boolean isBiconditional(LogicTree lt){
        if(lt.operator == '=' && lt.children.size() == 2) return true;
        return false;
    }

   

    /**
        Checks if the current node, if it is an operator node, if it negated
        Used later on for moving negations inward
     */
    public static boolean operatorIsNegated(LogicTree lt){
        if(lt.negated && operators.contains("" + lt.operator) ) return true;
        return false;
    }

    /**
        Negates the current node,
        1. If current node is not negated, then it will set negated to true
        2. If current node is negated, then it will set negated to false
     */
    public static boolean negateNode(LogicTree lt){
        lt.negated = !lt.negated;
        return lt.negated;
    }

    /**
        Checks if the current node, if it is an OR node,
        if it has any children nodes that are AND nodes, as such the or can be distributed over
        Used later for the distribution step of the algorithm
     */
    public boolean isDistributable(LogicTree lt){
        if(lt.operator == '|' && lt.children != null) {
            for(LogicTree c : lt.children){
                if(c.operator == '&'){
                    return true;
                }
            }
        }

        return false;
    }

    /**
        This function converts any implications from p > q to ~p | q
        Also converts and biconditionals from p = q to (~p | q) & (q | ~p)

        Note: p and q just refer to 

        Used later for the removal of implication step
    */
   public void removeImplication(LogicTree lt){
        if(isImplication(lt)){
            lt.operator = '|';
            //this is the left node, so in this case, it would be equivalent to p
            negateNode(lt.children.get(0));
        } else if(isBiconditional(lt)){
            // we want to temporarily store what the children are so we can appropriately put them back
            // into place after we change the operators, as we are now effectively making 4 subtrees
            // from the original two
            LogicTree pNode = lt.children.get(0);
            
            LogicTree qNode = lt.children.get(1);

            //first thing we must do is set the current operator to &
            lt.operator = '&';

            //next we will remove pNode and qNode from the children arraylist

            lt.children.clear();


            //next we create the copies of the pNode and qNode
            LogicTree pNodeCopy = createDeepCopy(pNode);
            LogicTree qNodeCopy = createDeepCopy(qNode);

            //at this point, we must negate p and copyQ so that we get (~p | q) & (~q | p)

            negateNode(pNode);
            negateNode(qNodeCopy);

            //next we will create the nodes that will hold these new
            LogicTree pToQNode = new LogicTree(lt,pNode,qNode,'|', false);
            

            LogicTree qToPNode = new LogicTree(lt,pNodeCopy,qNodeCopy,'|', false);

            //now will will make pToQNode and qToPNode the children of the this current node
            lt.children.add(pToQNode);
            lt.children.add(qToPNode);

        }
   }

   /**
    Because of the way I have set up biconditionals, I need to create deep copies
    of both the pNode and the qNode.

    Thankfully, because I made each node actually store the input from which it was derived, I can

    Hence this method will return a deep copy except for the parent,
    */
   public static LogicTree createDeepCopy(LogicTree lt){
    LogicTree newLt;

    // if the input logic tree is a leaf, then we must call the special constructor
        if(lt.children == null || lt.children.size() == 0){
            return new LogicTree(null,lt.variable,lt.negated);

        } else {
        //if we have entered this that means we should be in an operator node
        //hence, we must createDeepCopy of not only this node, but make sure that for each node
        //we createDeepCopy of all the corresponding children too
        //thankfully this is done by default by passing in the input string

            newLt = new LogicTree(null,lt.input);
            newLt.negated = lt.negated;

            return newLt;
            
        }

   }

    /**
        This method is used to help propogate negation inwards
        This is done by removing the negation from the current node
        And then negating the children nodes and switching to the appropriate operator
     */
   public static void deMorgan(LogicTree lt){
        if(operatorIsNegated(lt) && lt.operator == '&'){
            lt.negated = false;
            lt.operator = '|';
            for(LogicTree c: lt.children){
                negateNode(c);
            }      

        } else if (operatorIsNegated(lt) && lt.operator == '|'){
            lt.negated = false;
            lt.operator ='&';
            for(LogicTree c: lt.children){
                negateNode(c);
            }
        }
    }
}
