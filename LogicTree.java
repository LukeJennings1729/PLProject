import java.util.*;

import java.io.*;
/**
 * Write a description of class LogicTree here.
 *
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

    public LogicTree(LogicTree parent, char var, boolean negated){

        this.parent = parent;
        this.variable = var;
        this.negated = negated;
        this.operator = ';';

        this.children = null;
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
}
