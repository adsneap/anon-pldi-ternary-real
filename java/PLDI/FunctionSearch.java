package PLDI;

import java.math.BigInteger;
import java.util.ArrayList;

/*
 * The goal of this file is to search FunctionCodes for a given Predicate, 
 * up to a given precision level. For example, we could search for a value
 * of x such that x^2 + 1 >= 13, up to precision 10. This would return a
 * value of x such that x^2 + 1 >= 13, correct up to 2^-10 (or 1/1024).
 * 
 * An abstract view of search on Ternary Boehm Reals is as follows:
 *  - Given a predicate P, a precision level n, and a range [a,b], find a
 *    value x such that P(x) is true, correct up to 2^-n, and a <= x <= b.
 *  - Begin by discretizing the range [a,b] into ? intervals, placing
 *    them into an array.
 *  - For each interval in the array, build a Ternary Boehm Real that 
 *    passes through the interval, and check if the predicate is true on
 *    that interval.
 *  - If a real is found such that the predicate is true, return that real, 
 *    otherwise indicate that no such real exists.
 *
 * This is implemented in the following code. The methods return values
 * so that the search can be done in a step-by-step manner, and the
 * intermediate results can be inspected, but a search can also be done
 * in one step. It also provides timing information, so that the time
 * taken to do the search can be analysed, as well as the number of
 * intervals that were checked.
 */

public class FunctionSearch {
    FunctionCode function; // The function to search
    PredicateCode predicate; // The predicate to search for
    int epsilon; // The precision level
    VariableIntervalCode compactInterval; // The range [a,b]
    Boolean found = false; // Whether a real has been found
    ArrayList<SpecificIntervalCode> frontier = new ArrayList<SpecificIntervalCode>(); // The intervals to check
    SpecificIntervalCode answer; // The interval that contains the real
    int intervalsChecked = 0; // The number of intervals checked
    long timeTaken; // The time taken to do the search

    // Constructor
    public FunctionSearch(FunctionCode function, PredicateCode predicate, VariableIntervalCode compactInterval) {
        this.function = function;
        this.predicate = predicate;
        this.compactInterval = compactInterval;
        this.epsilon = predicate.delta;
    }

    // Getters
    public PredicateCode getPredicate() {
        return predicate;
    }

    public Boolean getFound() {
        return found;
    }

    public ArrayList<SpecificIntervalCode> getFrontier() {
        return frontier;
    }

    public int getFrontierSize() {
        return frontier.size();
    }

    public SpecificIntervalCode getAnswer() {
        return answer;
    }

    public TernaryBoehmReal getAnswerReal() {
        return new TernaryBoehmReal(answer.getLeftEndpoint());
    }

    public Double getAnswerDouble(Integer precision) {
        TernaryBoehmReal tbr = getAnswerReal();
        return tbr.toDouble(precision);
    }

    public Double getAnswerDouble() {
        return getAnswerDouble(epsilon);
    } 

    public long getTimeTaken() {
        return timeTaken;
    }

    public long getTimeTakenMillis() {
        return timeTaken / 1000000;
    }

    public int getIntervalsChecked() {
        return intervalsChecked;
    }

    // Methods

    // Discretize the range [a,b] into 2^Epsilon intervals
    public ArrayList<SpecificIntervalCode> discretize() {
        SpecificIntervalCode initial = new SpecificIntervalCode(compactInterval);
        int delta = function.getUniformContinuityOracle(initial).apply(epsilon).get(0);
        frontier = SpecificIntervalCode.discretize(delta, compactInterval);
        return frontier; 
    } 

    Boolean check() {
        SpecificIntervalCode tmp = frontier.get(0);
        TernaryBoehmReal x = new TernaryBoehmReal(tmp.getLeftEndpoint());
        if (predicate.getPredicate().apply(x)) {
            return true;
        } else {
            return false;
        }
    }

    Boolean search() {
        long startTime = System.nanoTime();
        discretize();
        while (frontier.size() > 0 && !found) {
            intervalsChecked++;
            if (check()) {
                found = true;
                answer = frontier.get(0);
                break;
            } else {
                frontier.remove(0);
            }
        }
        timeTaken = System.nanoTime() - startTime;
        return found;
    }

    Boolean search_verbose(int eps) {
        search();
        System.out.println("Intervals checked: " + intervalsChecked);
        System.out.println("Time taken: " + getTimeTakenMillis() + "ms");
        if (found) {
            System.out.println("Real found: " + getAnswerDouble(eps));
        } else {
            System.out.println("No real found");
        }
        return found;
    }

    Boolean search_verbose() {
        return search_verbose(epsilon);
    }

}

