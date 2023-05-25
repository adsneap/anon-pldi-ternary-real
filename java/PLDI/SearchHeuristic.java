package PLDI;

import java.util.ArrayList;

/*
 * While the search in Search2.java is correct, it is not very efficient.
 * This is because it checks every interval in the range [a,b] without any 
 * considerations of which intervals are more likely to contain the real.
 * 
 * A search heuristic is a way of deciding which intervals to check first.
 * Note that heuristics do not _guarantee_ that we will find the real faster. 
 * We are still searching for a real that satisfies the predicate, and 
 * sometimes heuristics will vastly improve the search time, but sometimes
 * they will not. 
 * 
 * A search guided by a heuristic adds potentially two more steps to the search:
 * - A sorting step, where the intervals are sorted according to the heuristic
 *   (this can be done in any search setting)
 * - A refining step (which can only be done in a setting where we can check 
 *   whether a predicate is true on intervals with lower precision than the
 *   precision level we are searching for). If we begin the search at a 
 *   lower precision level, then for each interval we can assign a score to 
 *   the intervals subintervals, and place the subintervals in the frontier
 *   according to their score. 
 * 
 * The following code provides a framework for implementing search heuristics.
 * The idea is that a search heuristic is a class that extends SearchHeuristic,
 * and implements the abstract method score. The score method takes a
 * SpecificIntervalCode and returns a score for that interval. The search
 * heuristic can then be used in a search by calling the sort method on the
 * frontier, and then calling the refine method on the frontier.
 */
abstract class SearchHeuristic extends FunctionSearch {

    public SearchHeuristic(FunctionCode function, PredicateCode predicate,
            VariableIntervalCode compactInterval) {
        super(function, predicate, compactInterval);
        
    }

    abstract ArrayList<SpecificIntervalCode> sort();
    abstract ArrayList<SpecificIntervalCode> refine();
    abstract ArrayList<SpecificIntervalCode> insert();

    Boolean search() {
        long startTime = System.nanoTime();
        discretize();
        while (!frontier.isEmpty() && !found) {
            intervalsChecked++;
            if (check()) {
                found = true;
                answer = frontier.get(0);
                break;
            } else {
                frontier.remove(0);
                refine();
                insert();
            }
            sort();
        }
        timeTaken = System.nanoTime() - startTime;
        return found;

    }
    
}

/*
 * This heuristic works by choosing a random interval from the frontier to 
 * check next.
 */
class SearchRandom extends SearchHeuristic {

    public SearchRandom(FunctionCode function, PredicateCode predicate,
            VariableIntervalCode compactInterval) {
        super(function, predicate, compactInterval);
    }

    ArrayList<SpecificIntervalCode> sort() {
        int index = (int) Math.floor((Math.random() * frontier.size()));
        SpecificIntervalCode interval = frontier.remove(index);
        frontier.add(0, interval);
        return frontier;
    }

    ArrayList<SpecificIntervalCode> refine() {
        return frontier;
    }

    ArrayList<SpecificIntervalCode> insert() {
        return frontier;
    }

}
