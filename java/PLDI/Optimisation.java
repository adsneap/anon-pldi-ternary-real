package PLDI;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/*
* This file implements optimisation methods, including minimisation and 
* maximisation of polynomials on a given interval. We do not use the 
* Search.java file because we don't use predicate codes to optimise.
* 
* The following abstract class is used to implement the optimisation methods.
* - We begin with a single interval and a function code. 
* - The uniform continuity oracle tells us which level of precision we need to
*   search in order to find the minimum. We begin the search at the given level 
*   of precision. If the compact interval is more precise than the uniform
*   continuity oracle, then we return the compact interval. Otherwise, we
*   refine the compact interval and repeat the process.
* - The refinement process is as follows: we split the compact interval into
*   two subintervals and put them back into the frontier, potentially in a 
*   certain order according to some heuristic.
*/
abstract class Optimisation {
    FunctionCode Function;
    VariableIntervalCode CompactInterval;
    int Epsilon;
    ArrayList<SpecificIntervalCode> History;
    ArrayList<Pair<SpecificIntervalCode,VariableIntervalCode>> Frontier;
    VariableIntervalCode Answer;
    VariableIntervalCode CurrentMin;
    
    abstract void initialise();
    abstract void sort();
    abstract void refine();
    abstract void insert();
    abstract void optimise();

}

/*
 * This class implements a naive optimisation method. We simply split the 
 * interval into intervals on the precision level dictated by the uniform
 * continuity oracle, and exhaustively evaluate the function code on each
 * interval, returning the minimum.
 */
class NaiveMinimisation extends Optimisation {
    NaiveMinimisation(FunctionCode f, VariableIntervalCode i, int e) {
        Function = f;
        CompactInterval = i;
        Epsilon = e;
        History = new ArrayList<SpecificIntervalCode>();
        Frontier = new ArrayList<Pair<SpecificIntervalCode,VariableIntervalCode>>();
    }

    void initialise() {
        SpecificIntervalCode initial = new SpecificIntervalCode(CompactInterval);
        int delta = Function.getUniformContinuityOracle(initial).apply(Epsilon).get(0);
        BigInteger current = initial.downLeft(delta - initial.getPrec()).getCode();
        BigInteger end = initial.downRight(delta - initial.getPrec()).getCode();
        while (current.compareTo(end) < 1) {
            SpecificIntervalCode si = new SpecificIntervalCode(new DyadicCode(current,delta));
            VariableIntervalCode vi = si.getVariableIntervalCode();
            VariableIntervalCode fvi = Function.apply(vi);
            Frontier.add(new Pair<>(si,fvi));
            current = current.add(BigInteger.TWO);
        }  
        if (Frontier.isEmpty()) {
            CurrentMin = CompactInterval;
            Answer = CompactInterval;
        } else {
            CurrentMin = Frontier.get(0).getSnd();
            Answer = Frontier.get(0).getFst().getVariableIntervalCode();
        }
    }

    void sort() {
        // Do nothing
    }

    void refine() {
        Pair<SpecificIntervalCode,VariableIntervalCode> p = Frontier.get(0);
        SpecificIntervalCode si = p.getFst();
        VariableIntervalCode vi = p.getSnd();
        Frontier.remove(0);
        if (SpecificIntervalCode.lessThan(vi, CurrentMin)) {
            CurrentMin = vi;
            Answer = si.getVariableIntervalCode();
        }
        
    }

    void insert() {
        // Do nothing
    }

    void optimise() {
        initialise();
        while (!Frontier.isEmpty()) {
            sort();
            refine();
        }
    }

    public String toString() {
        return Answer.toString();
    }

    public SpecificIntervalCode getAnswer() {
        return new SpecificIntervalCode(Answer);
    }

    public TernaryBoehmReal toReal() {
        return new TernaryBoehmReal(getAnswer());
    }

    public Double toDouble(Integer p) {
        TernaryBoehmReal x = toReal();
        return x.toDouble(p);
    }
}

/*
 * This class implements some examples of naive minimisation.
 * 
 * Example 1: Minimise the function x^2 on the interval [-2,2] with epsilon = 4.
 *            The answer should be 0.
 * 
 * Example 2: Minimise the function (x - 1)^2 on the interval [-2,2] with epsilon = 4.
 *            The answer should be 1.
 * 
 * Example 3: Minimise the function x^6 + x^5 - x^4 + x^2 on the interval [-2,2] 
 *            with epsilon = 1. The answer should be -1.1959.
 *            Due to the nature of the naive optimisation method, this function
 *            crashes the program if epsilon is too large.
 */
class TestNaiveMinimisation {
    
    static void example1() {
        VariableIntervalCode i = new VariableIntervalCode(BigInteger.valueOf(-2),BigInteger.valueOf(2),0);
        FunctionCode F = FunctionCode.unaryPolynomial(Arrays.asList(
            new Pair<>(new TernaryBoehmReal(1),2)
        ));
        NaiveMinimisation o = new NaiveMinimisation(F,i,4);
        o.optimise();
        System.out.println(o.toDouble(0));
    }

    static void example2() {
        VariableIntervalCode i = new VariableIntervalCode(BigInteger.valueOf(-2),BigInteger.valueOf(2),0);
        FunctionCode F = FunctionCode.unaryPolynomial(Arrays.asList(
            new Pair<>(new TernaryBoehmReal(1),2),
            new Pair<>(new TernaryBoehmReal(-2),1),
            new Pair<>(new TernaryBoehmReal(1),0)
        ));
        NaiveMinimisation o = new NaiveMinimisation(F,i,8);
        o.optimise();
        System.out.println(o.toDouble(0));
    }


    /*
     * The above example timed
     */
    static void timedexample2() {
        VariableIntervalCode i = new VariableIntervalCode(BigInteger.valueOf(-2),BigInteger.valueOf(2),0);
        FunctionCode F = FunctionCode.unaryPolynomial(Arrays.asList(
            new Pair<>(new TernaryBoehmReal(1),2),
            new Pair<>(new TernaryBoehmReal(-2),1),
            new Pair<>(new TernaryBoehmReal(1),0)
        ));
        NaiveMinimisation o = new NaiveMinimisation(F,i,8);
        long startTime = System.nanoTime();
        o.optimise();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;   
        System.out.println(duration);
        System.out.println(o.toDouble(0));
    }

    static void example3() {
        VariableIntervalCode i = new VariableIntervalCode(BigInteger.valueOf(-2),BigInteger.valueOf(2),0);
        FunctionCode F = FunctionCode.unaryPolynomial(Arrays.asList(
            new Pair<>(new TernaryBoehmReal(1),6),
            new Pair<>(new TernaryBoehmReal(1),5),
            new Pair<>(new TernaryBoehmReal(-1),4),
            new Pair<>(new TernaryBoehmReal(1),2)
        ));
        NaiveMinimisation o = new NaiveMinimisation(F,i,1);
        o.optimise();
        System.out.println(o.toReal());
    }
}

/*
 * Now we implement a more sophisticated optimisation method. We eliminate 
 * intervals that are eclipsed by other intervals, meaning that the search 
 * space is reduced. 
 */
class SplitMinimisation extends NaiveMinimisation {


    ArrayList<VariableIntervalCode> Answers;
    int delta;
    VariableIntervalCode CurrentMin;
    int index;

    SplitMinimisation(FunctionCode f, VariableIntervalCode i, int e) {
        super(f,i,e);
        Function = f;
        CompactInterval = i;
        Epsilon = e;
        History = new ArrayList<SpecificIntervalCode>();
        Answers = new ArrayList<VariableIntervalCode>();
        Frontier = new ArrayList<Pair<SpecificIntervalCode,VariableIntervalCode>>();
    }

    void initialise() {
        SpecificIntervalCode initial = new SpecificIntervalCode(CompactInterval);
        delta = Function.getUniformContinuityOracle(initial).apply(Epsilon).get(0);
        VariableIntervalCode finitial = Function.apply(CompactInterval);
        Frontier.add(new Pair<>(initial,finitial));
        CurrentMin = finitial;
        Answer = CompactInterval;
    }

    /*
     * This function returns true if the given interval is eclipsed by any of
     * the intervals in the frontier.
     */
    boolean eclipsed(VariableIntervalCode vi) {
        for (Pair<SpecificIntervalCode,VariableIntervalCode> p : Frontier) {
            if (VariableIntervalCode.eclipses(vi,p.getSnd())) {
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
            if (VariableIntervalCode.eclipses(vi,v)) {
                return true;
            }
        }
        return false;
    }
    

    void refine() {
        index = (int) (Math.random() * Frontier.size());
        // Choose a random interval from the frontier
        Pair<SpecificIntervalCode,VariableIntervalCode> p = Frontier.remove(index);
        SpecificIntervalCode si = p.getFst();
        VariableIntervalCode vi = p.getSnd();
        if (vi.getPrec() >= Epsilon && SpecificIntervalCode.lessThanRight(vi, CurrentMin)) {
            CurrentMin = vi;
            Answer = si.getVariableIntervalCode();
        } 

        if (si.getPrec() < delta) {
            SpecificIntervalCode left = si.downLeft();
            SpecificIntervalCode right = si.downRight();
            VariableIntervalCode fleft = Function.apply(left.getVariableIntervalCode());
            VariableIntervalCode fright = Function.apply(right.getVariableIntervalCode());
            Pair<SpecificIntervalCode,VariableIntervalCode> leftfleft = new Pair<>(left,fleft);
            Pair<SpecificIntervalCode,VariableIntervalCode> rightfright = new Pair<>(right,fright);
            System.out.println(Frontier.size());
            Frontier.removeIf(y -> VariableIntervalCode.eclipses(fleft,y.getSnd()) || VariableIntervalCode.eclipses(fright,y.getSnd()));

            System.out.println(Frontier.size());

            //add left if not eclipsed by frontier or right, and right if not eclipsed by frontier or left
            if (!eclipsed(fleft , fright)) {
                Frontier.add(leftfleft);
                Answers.add(fleft);

            }
            if (!eclipsed(fright , fleft)) {
                Frontier.add(rightfright);
                Answers.add(fright);
            }
            
            
        } 
           
        
    }

    // void sort() {
    //     Collections.shuffle(Frontier); 
    // }

}

/*
 * The following class searches the previous examples using the eclipse
 * optimisation method.
 */
class TestSplitMinimisation {
    
    static void example1() {
        VariableIntervalCode i = new VariableIntervalCode(BigInteger.valueOf(-2),BigInteger.valueOf(2),0);
        FunctionCode F = FunctionCode.unaryPolynomial(Arrays.asList(
            new Pair<>(new TernaryBoehmReal(1),2)
        ));
        SplitMinimisation o = new SplitMinimisation(F,i,4);
        o.optimise();
        System.out.println(o.toDouble(0));
    }

    static void example2() {
        VariableIntervalCode i = new VariableIntervalCode(BigInteger.valueOf(-2),BigInteger.valueOf(2),0);
        FunctionCode F = FunctionCode.unaryPolynomial(Arrays.asList(
            new Pair<>(new TernaryBoehmReal(1),2),
            new Pair<>(new TernaryBoehmReal(-2),1),
            new Pair<>(new TernaryBoehmReal(1),0)
        ));
        SplitMinimisation o = new SplitMinimisation(F,i,4);
        o.optimise();
        System.out.println(o.toDouble(5));
    }

    static void example3() {
        VariableIntervalCode i = new VariableIntervalCode(BigInteger.valueOf(-2),BigInteger.valueOf(2),0);
        FunctionCode F = FunctionCode.unaryPolynomial(Arrays.asList(
            new Pair<>(new TernaryBoehmReal(1),6),
            new Pair<>(new TernaryBoehmReal(1),5),
            new Pair<>(new TernaryBoehmReal(-1),4),
            new Pair<>(new TernaryBoehmReal(1),2)
        ));
        SplitMinimisation o = new SplitMinimisation(F,i,5);
        o.optimise();
        System.out.println(o.toDouble(5));
    }

    //same as example3 but timed in milliseconds
    static void timedexample3() {
        VariableIntervalCode i = new VariableIntervalCode(BigInteger.valueOf(-2),BigInteger.valueOf(2),0);
        FunctionCode F = FunctionCode.unaryPolynomial(Arrays.asList(
            new Pair<>(new TernaryBoehmReal(1),6),
            new Pair<>(new TernaryBoehmReal(1),5),
            new Pair<>(new TernaryBoehmReal(-1),4),
            new Pair<>(new TernaryBoehmReal(1),2)
        ));
        SplitMinimisation o = new SplitMinimisation(F,i,5);
        long startTime = System.nanoTime();
        o.optimise();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;   
        System.out.println("Time taken: " + duration + " milliseconds");
        System.out.println(o.toDouble(5));
    }
}
