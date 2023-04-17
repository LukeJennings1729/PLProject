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

    /**
     * Contrustor used for parsing the initial input from the file
     * 
     * @param parent 
     * @param inputString String input from the initial scanner to be translated into the tree
     */
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
    public LogicTree(LogicTree parent, LogicTree pNode, LogicTree qNode, char operator, char variable, boolean negated){
        this.children = new ArrayList<LogicTree>();

        children.add(pNode);
        children.add(qNode);
        this.operator = operator;
        this.negated = negated;
        this.parent = parent;
        this.variable = variable;
        pNode.parent = this;
        qNode.parent = this;
    }



    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        if(this.negated) sb.append("~");
        if(this.children != null){
            sb.append("(");
            sb.append(this.children.get(0).toString());
        }
        if(this.operator != ';'){

            sb.append(operator);
        } else {
            sb.append(variable);
        }

        if(this.children != null){

            sb.append(this.children.get(1).toString());
            sb.append(")");
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
    public static boolean isDistributable(LogicTree lt){
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
    public static void removeImplication(LogicTree lt){
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
            LogicTree pToQNode = new LogicTree(lt,pNode,qNode,'|',';' ,false);

            LogicTree qToPNode = new LogicTree(lt,pNodeCopy,qNodeCopy,'|',';', false);
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

        } else if (lt.input != null) {
            //if we have entered this that means we should be in an operator node
            //hence, we must createDeepCopy of not only this node, but make sure that for each node
            //we createDeepCopy of all the corresponding children too
            //thankfully this is done by default by passing in the input string

            newLt = new LogicTree(lt.parent,lt.input);
            newLt.negated = lt.negated;

            return newLt;

        } else {
            // if we have entered this, that means I messed up when initially making this
            // this is the actual correct way for making a deep copy, because I was originally to lazy too
            return new LogicTree(lt.parent,createDeepCopy(lt.children.get(0)),createDeepCopy(lt.children.get(1)),lt.operator,lt.variable,lt.negated);

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

    /**
     * This function should remove all implications from our logictree
     * By removing the implication from the current node, and then recursively calling
     * the function on the children
     * 
     * Used for the first step of the CNF conversion process
     * 
     * (VERIFIED IT WORKS)
     * 
     */
    public void removeAllImplications(){
        removeImplication(this);
        if(this.children != null){
            for(LogicTree c: this.children){
                c.removeAllImplications();
            }
        }
    }

    /**
     *
     * This method removes all negation in non-leaf and move them inward
     *  
     * Hence this fulfills the second step of the CNF conversion algorithm
     * */
    public void removeNegations(){
        deMorgan(this);
        if(this.children != null){
            for(LogicTree c: this.children){
                c.removeNegations();
            }
        }
    }

    /**
     * This method takes the current tree, and distributes ORs over ANDs
     * And then is recursively applied to the entire tree
     * 
     * 
     * */
    public void applyDistribution(){
        if(isDistributable(this)){
            distributeOr(this);
        }

        //then we must apply it to the children as well
        if(this.children != null){
            for(LogicTree c: this.children){
                c.applyDistribution();
            }
        }

        //finally we must double check if the tree after distribution is able to
        //be distributed over again
        if(isDistributable(this)){
            distributeOr(this);
        }

    }

    /**
     * This method actually distributes the or across the current node is able
     */
    public static void distributeOr(LogicTree lt){
        //first when we distribute the OR, we must move the and to be outwards
        lt.operator = '&';

        //the case if both children are Ands is different than if there is only one and
        //as such we need to make sure we structure the tree properly without accidently
        //putting more/less variables in a clause than there should be

        int countAndsInChildren = 0;
        for(LogicTree c: lt.children){
            if(c.operator == '&'){
                countAndsInChildren++;
            }
        }

        if(countAndsInChildren == 2){
            // this means we are in a case where we have (p & q) | (r & s)
            // thus we must make the output become (p | r) & (p | s) & (q | r) & (q | s)
            /*
             * Thus the way we get to this state is by creating the tree like this
             *              &
             *         &            &
             *      |    |      |       |
             *   (p r) (p s)  (q r)     (q s)
             */
            //next we must make 2 subtrees and then distribute on the children of those 2 subtrees

            //

            LogicTree pqNodes = lt.children.get(0);
            LogicTree rsNodes = lt.children.get(1);

            LogicTree pNode = pqNodes.children.get(0);
            LogicTree pNodeCopy = createDeepCopy(pNode);
            LogicTree qNode = pqNodes.children.get(1);
            LogicTree qNodeCopy = createDeepCopy(qNode);

            LogicTree rNode = rsNodes.children.get(0);
            LogicTree rNodeCopy = createDeepCopy(rNode);
            LogicTree sNode = rsNodes.children.get(1);
            LogicTree sNodeCopy = createDeepCopy(sNode);

            //first we will start from the bottom of this tree
            //creating the or nodes that will hold the p,q,r,s values
            LogicTree prNode = new LogicTree(null,pNode,rNode,'|',';',false);
            LogicTree psNode = new LogicTree(null,pNodeCopy,sNode,'|',';',false);
            LogicTree qrNode = new LogicTree(null,qNode,rNodeCopy,'|',';',false);
            LogicTree qsNode = new LogicTree(null,qNodeCopy,sNodeCopy,'|',';',false);

            //because we just created a lot more or nodes, we must also check these for distribution

            //now we must combine these nodes together wit and nodes
            LogicTree pSubTree = new LogicTree(null,prNode,psNode,'&',';',false);
            LogicTree qSubTree = new LogicTree(null,qrNode, qsNode,'&',';',false);

            //finally we many the two subtrees the new children of the current node
            lt.children.clear();
            lt.children.add(pSubTree);
            lt.children.add(qSubTree);

        } else if(countAndsInChildren == 1){
            //this means that the and appears in the first subtree, so the structure looks like this
            // ((p & q) | r) which becomes ((p | r) & (q | r))
            if(lt.children.get(0).operator == '&'){
                LogicTree pqNode = lt.children.get(0);
                LogicTree rNode = lt.children.get(1);
                LogicTree pNode = pqNode.children.get(0);
                LogicTree qNode = pqNode.children.get(1);

                LogicTree rNodeCopy = createDeepCopy(rNode);

                LogicTree prNode = new LogicTree(null,pNode,rNode,'|',';',false);
                LogicTree qrNode = new LogicTree(null,qNode,rNodeCopy,'|',';',false);

                lt.children.clear();
                lt.children.add(prNode);
                lt.children.add(qrNode);

            } else if(lt.children.get(1).operator == '&'){
                //this means that the and appears in the second subtree, so the structure looks like
                // (p | (q & r)) which becomes ((p | q) & (p | r))

                LogicTree pNode = lt.children.get(0);
                LogicTree qrNode = lt.children.get(1);
                LogicTree qNode = qrNode.children.get(0);
                LogicTree rNode = qrNode.children.get(1);

                LogicTree pNodeCopy = createDeepCopy(pNode);

                LogicTree pqNode = new LogicTree(null,pNode,qNode,'|',';',false);
                LogicTree prNode = new LogicTree(null,pNodeCopy,rNode,'|',';',false);

                lt.children.clear();
                lt.children.add(pqNode);
                lt.children.add(prNode);
            }

        }
    }

    /**
     * This method will return an ArrayList<ArrayList<String>> that which is naively equivalent to
     * the CNF of the actually logical input
     * 
     * This will not perform any kind of removing tautologies or identifying if any clauses are logically equivalent
     * 
     * If we did all the steps before this properly, all the or statements should be as deep down as possible
     * This means if we find an or, we can create a list, to which whenever we encounter a leaf node, we can add its variable to the list
     * 
     */
    public ArrayList<ArrayList<String>> naiveCNF(){
        ArrayList<ArrayList<String>> clauses = new ArrayList<ArrayList<String>>();

        //if the current tree is an and, that means we must call naiveCNF on its children
        if(this.operator == '&'){
            for(LogicTree c: this.children){
                clauses.addAll(c.naiveCNF());
            }
        } else if (this.operator == '|'){
            //if we hit an or node, that means we have entered inside a clause, hence we must add subtree clause to the list of clauses
            ArrayList<String> newClause = new ArrayList<String>();
            clauses.add(subtreeClause(this,newClause));
        } else {
            // if we hit this, that means that we have performed an and operation with a child node in one side
            ArrayList<String> newClause = new ArrayList<String>();
            if(this.negated){
                newClause.add("~"+this.variable);
            } else {
                newClause.add(""+this.variable);
            }
            clauses.add(newClause);
        }
       
        return clauses;
    }

    
  

    /**
     * This method, if it finds a subtree only containing or statements, will return a clause containing all the variables
     * This method will also make sure not to return tautologies and duplicates
     */
    public static ArrayList<String> subtreeClause(LogicTree lt,ArrayList<String> clause){
        //first we must check that th

        //base case, if we have reached a leaf node, then we add the variable to the clause
        if(lt.children == null){
            if(lt.negated){ 
                
                if(!clause.contains("~ + lt.variable")) clause.add("~" + lt.variable);
            } else {
                if(!clause.contains(""+lt.variable)) clause.add("" + lt.variable);
            }
        
        // the situation we are not in a base case, we must make sure that we are only traversing over or statements, otherwise we are gonna break apart
        // the CNF, thus making the whole function useless
        } else if(lt.operator == '|'){
            for(LogicTree c: lt.children){
                subtreeClause(c,clause);
            }
        }
        return clause;
    }

    

}

