# PLProject
Project #2 for Intro to AI course for CNF converter and PL resolution algorithm

Part 1: CNFConverter

Program is executed using the main method of the CNFConverter.java file, with string args simply being the name of the input file (path if not in same repository)

In system.out, there will be a print out of the
1. Propositional logic string from the input file
2. Propositional logic with all implications and biconditionals removed
3. PL with all negations moved to the leaf nodes
4. PL with all OR statements distributed across ANDs, effectively putting it in a naive CNF form

The output clauses are then sorted by ASCII value (with the commas included, if this is a problem, it would take me 3 minutes to fix, you just didn't respond to my email so I assumed it was ok as it is)

LogicTree.java is where most of the computation is where all the support functions are occuring

Part 2: Resolution2

Program is executed using the main method of the Resolution2.java file, with string args being the name of the input file (path if not in same repository)

For both parts all output is given in a file named output + "inputFileName" + (random integer from 0 to 99).txt

Some helper functions such as the isTautology and intersection methods refused to link, so they are duplicated in both parts 1 and 2, however, these should be self-explanitory in their functionality.

Please reach out to me if there are any issues

Thanks,
Luke Jennings