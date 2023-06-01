package PLDI;

import java.math.BigInteger;
import java.util.ArrayList;

/*
 * This file implements optimisation functions, in particular the minimisation
 * of functions over intervals.
 * 
 * We can minimise functions to arbitrary precision, returning the interval
 * which minimises the output of the function over the input interval.
 * 
 * It provides timing information, so that the time taken to do the search can
 * be analysed, as well as the number of intervals that were checked. It also
 * provides an array of intermediate minimum intervals and outputs.
 */
public class Optimisation2 {
    FunctionCode function; // The function to search
    int epsilon; // The precision level
    VariableIntervalCode compactInterval; // The range [a,b]
    SpecificIntervalCode input; // The interval that contains the minimum
    VariableIntervalCode output; // The output of the function on the minimum interval
    int intervalsChecked = 0; // The number of intervals checked
    long timeTaken; // The time taken to do the search
    ArrayList<VariableIntervalCode> answers = new ArrayList<VariableIntervalCode>(); // Intermediate minimum intervals

    // The intervals to check with their corresponding outputs
    ArrayList<Pair<SpecificIntervalCode,VariableIntervalCode>> frontier = new ArrayList<Pair<SpecificIntervalCode,VariableIntervalCode>>(); 

    // Constructor
    public Optimisation2(FunctionCode function, VariableIntervalCode compactInterval, int epsilon) {
        this.function = function;
        this.compactInterval = compactInterval;
        this.epsilon = epsilon;
    }

    // Getters
    public ArrayList<Pair<SpecificIntervalCode,VariableIntervalCode>> getFrontier() {
        return frontier;
    }

    public int getFrontierSize() {
        return frontier.size();
    }

    public SpecificIntervalCode getAnswer() {
        return input;
    }

    public TernaryBoehmReal getAnswerReal() {
        return new TernaryBoehmReal(input.getLeftEndpoint());
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

    public VariableIntervalCode getOutput() {
        return output;
    }

    public TernaryBoehmReal getOutputReal() {
        return new TernaryBoehmReal(output.getLeftEndpoint());
    }

    public Double getOutputDouble(Integer precision) {
        TernaryBoehmReal tbr = getOutputReal();
        return tbr.toDouble(precision);
    }

    public Double getOutputDouble() {
        return getOutputDouble(epsilon);
    }

    public ArrayList<VariableIntervalCode> getAnswers() {
        return answers;
    }

    public String getAnswersString() {
        String s = "";
        for (VariableIntervalCode answer : answers) {
            TernaryBoehmReal tbr = new TernaryBoehmReal(answer.getLeftEndpoint());
            s += tbr.toDouble(epsilon) + "\n";
        }
        return s;
    }

    public String getAnswersOutputsString() {
        String s = "";
        for (VariableIntervalCode answer : answers) {
            VariableIntervalCode output = function.apply(answer);
            TernaryBoehmReal tbr = new TernaryBoehmReal(answer.getLeftEndpoint());
            TernaryBoehmReal ans = new TernaryBoehmReal(output.getLeftEndpoint());
            s += tbr.toDouble(epsilon) + " -> " + ans.toDouble(epsilon) + "\n";
        }
        return s;
    }

    public void printAnswersOutputsString() {
        for (VariableIntervalCode answer : answers) {
            VariableIntervalCode output = function.apply(answer);
            TernaryBoehmReal tbr = new TernaryBoehmReal(answer.getLeftEndpoint());
            TernaryBoehmReal ans = new TernaryBoehmReal(output.getLeftEndpoint());
            System.out.println(tbr.toDouble(epsilon) + " -> " + ans.toDouble(epsilon));
        }
    }

    public int getAnswersSize() {
        return answers.size();
    }

    public void removeEclipsedFromFrontier(VariableIntervalCode output) {
        for (int i = 0; i < frontier.size(); i++) {
            Pair<SpecificIntervalCode,VariableIntervalCode> intervalOutput = frontier.get(i);
            VariableIntervalCode output2 = intervalOutput.getSnd();
            if (VariableIntervalCode.eclipses(output, output2)) {
                frontier.remove(i);
                i--;
            }
        }
    }

    // Methods
    
    ArrayList<Pair<SpecificIntervalCode,VariableIntervalCode>> initialise() {
        SpecificIntervalCode initialInterval = new SpecificIntervalCode(compactInterval);
        int delta = function.getUniformContinuityOracle(initialInterval).apply(epsilon).get(0);
        ArrayList<SpecificIntervalCode> inputs = SpecificIntervalCode.discretize(delta, initialInterval.getVariableIntervalCode());

        for (SpecificIntervalCode interval : inputs) {
            VariableIntervalCode output = function.apply(interval.getVariableIntervalCode());
            frontier.add(new Pair<SpecificIntervalCode,VariableIntervalCode>(interval, output));
        }

        input = frontier.get(0).getFst();
        output = frontier.get(0).getSnd();

        return frontier;
    }

    Boolean check() {
        Pair<SpecificIntervalCode,VariableIntervalCode> intervalOutput = frontier.remove(0);
        SpecificIntervalCode interval = intervalOutput.getFst();
        VariableIntervalCode output = intervalOutput.getSnd();
        if (SpecificIntervalCode.lessThan(output, this.output)) {
            this.input = interval;
            this.output = output;
            answers.add(interval.getVariableIntervalCode());
            return true;
        } else {
            return false;
        }
    }

    void minimise() {
        long startTime = System.nanoTime();
        
        initialise();
        while (frontier.size() > 0) {
            intervalsChecked++;
            check();
        }
        timeTaken = System.nanoTime() - startTime;
    }

    void minimise_verbose() {
        long startTime = System.nanoTime();
        minimise();
        timeTaken = System.nanoTime() - startTime;
        System.out.println("Intervals checked: " + intervalsChecked);
        System.out.println("Time taken: " + getTimeTakenMillis() + "ms");
        System.out.println("Minimum interval: " + getAnswerDouble());
        System.out.println("Minimum output: " + getOutputDouble());
    }

    void minimise_verbose_number_answers() {
        minimise_verbose();
        System.out.println("Number of answers: " + getAnswersSize());
    }

    /*
     * This function returns true if the given interval is eclipsed by any of
     * the intervals in the frontier.
     */
    boolean eclipsed(VariableIntervalCode vi) {
        for (Pair<SpecificIntervalCode,VariableIntervalCode> p : frontier) {
            if (VariableIntervalCode.eclipses(p.getSnd() , vi)) {
                return true;
            }
        }
        return false;
    }

    /*
     * This function returns true if the given interval is eclipsed by any of
     * the frontier or another given interval.
     */
    boolean eclipsed(VariableIntervalCode vi, VariableIntervalCode vi2) {
        if (VariableIntervalCode.eclipses(vi,vi2)) {
            return true;
        }
        return eclipsed(vi);
    }

    /*
     * This function returns true if the given interval is eclipsed by any of
     * the intervals the answers.
     */
    boolean eclipsed(VariableIntervalCode vi, ArrayList<VariableIntervalCode> answers) {
        for (VariableIntervalCode v : answers) {
            if (VariableIntervalCode.eclipses(v,vi)) {
                return true;
            }
        }
        return false;
    }

}

/*
 * Unlike search, we can't use some heuristic to speed up the search when 
 * considering intervals at the precision level. This is because we need to
 * consider all intervals at the precision level, since we don't know which
 * one will be the minimum.
 * 
 * We can, however, use a heuristic to potentially speed up the search when 
 * considering intervals at a lower precision level. This is because we can
 * use the heuristic to rule out intervals that we know can't be the minimum.
 * Pruning the search space at lower precision levels can reduce the search
 * space at higher precision levels.
 * 
 * Applying a specific interval code to a function code returns a variable
 * interval code, which indicates the range of outputs that the function
 * could return on the input interval. If we have a variable interval code
 * which is "eclipsed" by the current minimum output, then we know that the
 * this code cannot contain the minimum output, and so we can remove it from
 * the frontier without considering any of its subintervals.
 */
class minimisation_heuristic extends Optimisation2 {
    int delta;
    SpecificIntervalCode initialInterval;

    public minimisation_heuristic(FunctionCode function, VariableIntervalCode compactInterval, int epsilon) {
        super(function, compactInterval, epsilon);
        initialInterval = new SpecificIntervalCode(compactInterval);
        delta = function.getUniformContinuityOracle(initialInterval).apply(epsilon).get(0);
    }

    // Methods

    /*
     * Initialise the frontier with the initial interval and its output.
     */
    ArrayList<Pair<SpecificIntervalCode,VariableIntervalCode>> initialise() {
        VariableIntervalCode input = initialInterval.getVariableIntervalCode();
        VariableIntervalCode output = function.apply(input);
        frontier.add(new Pair<SpecificIntervalCode,VariableIntervalCode>(initialInterval, output));
        return frontier;
    }

    /*
     * Check the next interval in the frontier. If it is eclipsed by the current
     * minimum output, then remove it from the frontier without considering any
     * of its subintervals. Otherwise, add its subintervals to the frontier.
     * 
     * If we are at the right precision level, then we can check if the output
     * is less than the current minimum output. If it is, then we have found
     * a new minimum output, and we can add the interval to the answers.
     */
    Boolean check() {
        Pair<SpecificIntervalCode,VariableIntervalCode> intervalOutput = frontier.remove(0);
        SpecificIntervalCode interval = intervalOutput.getFst();
        SpecificIntervalCode leftsi = interval.downLeft();
        SpecificIntervalCode rightsi = interval.downRight();
        VariableIntervalCode leftvi = function.apply(leftsi.getVariableIntervalCode());
        VariableIntervalCode rightvi = function.apply(rightsi.getVariableIntervalCode());

        Pair<SpecificIntervalCode,VariableIntervalCode> left = new Pair<SpecificIntervalCode,VariableIntervalCode>(leftsi, leftvi);
        Pair<SpecificIntervalCode,VariableIntervalCode> right = new Pair<SpecificIntervalCode,VariableIntervalCode>(rightsi, rightvi);

        if (!eclipsed(leftvi, rightvi) && !eclipsed(leftvi , answers)) {
            if (leftsi.getPrec() >= delta || leftvi.join_prime().getPrec() >= epsilon) {
                if (this.output == null) {
                    this.input = leftsi;
                    this.output = leftvi;
                    answers.add(leftvi);
                } else {
                    if (SpecificIntervalCode.lessThan(leftvi, this.output)) {
                        this.input = leftsi;
                        this.output = leftvi;
                        answers.add(leftvi);
                    } 
                }
            } else {
                frontier.add(left);
                removeEclipsedFromFrontier(leftvi);
            }
        }
            
        if (!eclipsed(rightvi, leftvi) && !eclipsed(rightvi , answers)) {
            if (rightsi.getPrec() >= delta || rightvi.join_prime().getPrec() >= epsilon) {
                if (this.output == null) {
                    this.input = rightsi;
                    this.output = rightvi;
                    answers.add(rightvi);
                } else {
                    if (SpecificIntervalCode.lessThan(rightvi, this.output)) {
                        this.input = rightsi;
                        this.output = rightvi;
                        answers.add(rightvi);
                    } 
                }
            } else {
                frontier.add(right);
                removeEclipsedFromFrontier(rightvi);
            }
        }

        return false;
        
    }

    void minimise() {
        long startTime = System.nanoTime();     
        initialise();
        while (frontier.size() > 0) {
            intervalsChecked++;
            check();
        }
        timeTaken = System.nanoTime() - startTime;
    }
    
}

/*
 * This class is a modification of the minimisation_heuristic class, which 
 * chooses a random interval from the frontier to check, rather than always
 * checking the first interval in the frontier.
 */
class minimisation_heuristic_random extends minimisation_heuristic {
    public minimisation_heuristic_random(FunctionCode function, VariableIntervalCode compactInterval, int epsilon) {
        super(function, compactInterval, epsilon);
    }

    void sort() {
        int index = (int) (Math.random() * frontier.size());
        Pair<SpecificIntervalCode,VariableIntervalCode> intervalOutput = frontier.remove(index);
        frontier.add(0, intervalOutput);
    }

    void minimise() {
        long startTime = System.nanoTime();     
        initialise();
        while (frontier.size() > 0) {
            intervalsChecked++;
            sort();
            check();
        }
        timeTaken = System.nanoTime() - startTime;
        
    }

}

/*
* This class is a modification of the minimisation_heuristic class, which
* ranks the intervals in the frontier by the size of their output intervals,
* and then checks the interval with the largest output interval.
* 
* Since all intervals in the frontier have the same input interval, the
* interval with the largest output interval is the one with the largest
* "derivative". After processing a few layers of precision, we would expect
* this heuristic to remove a large proportion of the frontier, and thus
* reduce the number of intervals that need to be checked.
*/ 
class differential_heuristic extends minimisation_heuristic {
    public differential_heuristic(FunctionCode function, VariableIntervalCode compactInterval, int epsilon) {
        super(function, compactInterval, epsilon);
    }

    boolean isWider(VariableIntervalCode fx, VariableIntervalCode fy) {
        if (fx.getPrec() > fy.getPrec()) {
            fy = fy.down(fx.getPrec() - fy.getPrec());
        } else if (fx.getPrec() < fy.getPrec()) {
            fx = fx.down(fy.getPrec() - fx.getPrec());
        }
        BigInteger fxWidth = fx.getRightCode().subtract(fx.getLeftCode());
        BigInteger fyWidth = fy.getRightCode().subtract(fy.getLeftCode());
        return fxWidth.compareTo(fyWidth) > 0;
    }

    // don't need to 'sort' as such, just move the interval with the largest output to the front
    void sort() {
        int index = 0;
        VariableIntervalCode largest = frontier.get(0).getSnd();
        for (int i = 1; i < frontier.size(); i++) {
            if (isWider(frontier.get(i).getSnd(), largest)) {
                index = i;
                largest = frontier.get(i).getSnd();
            }
        }
        Pair<SpecificIntervalCode,VariableIntervalCode> intervalOutput = frontier.remove(index);
        frontier.add(0, intervalOutput);
    }

    void minimise() {
        long startTime = System.nanoTime();     
        initialise();
        while (frontier.size() > 0) {
            intervalsChecked++;
            sort();
            check();
        }
        timeTaken = System.nanoTime() - startTime;
    }

}