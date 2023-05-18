package PLDI;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/* 
 * In general, we have FunctionCode's (which may be polynomials) which we 
 * wish to search up to a given precision level. We are searching the function
 * on a given compact interval using a given predicate, which is implemented 
 * as a PredicateCode. We are searching for a TernaryBoehmReal which satisfies
 * the predicate.
 * 
 * The strategy is to finitize the problem by discretising the compact
 * interval into a finite set of points, and then searching this finite set for
 * a point which satisfies the predicate. There are many different possible
 * search strategies, and we implement some of them here. Each search strategy
 * is a heuristic for finding a point which satisfies the predicate.
 * 
 * The following abstract class is implemented by all search strategies. The 
 * following ideas apply to all search strategies:
 * - We begin by discretising the compact interval (which is given as a 
 *   VariableIntervalCode) into a finite set of points corresponding to a 
 *   beginning precision level, using an ArrayList of SpecificIntervalCode's, 
 *   potentially sorting them according to some heuristic.
 * - We iterate through the ArrayList, and for each SpecificIntervalCode, we
 *   check whether the predicate is satisfied. If we are at the correct 
 *   precision level, we return a TernaryBoehmReal corresponding to this 
 *   SpecificIntervalCode. Otherwise, we refine the SpecificIntervalCode into
 *   into three subintervals, and add these to the ArrayList, potentially
 *   inserting them in the correct order according to some heuristic.
 * - We continue this process until we find a TernaryBoehmReal which satisfies
 *   the predicate, or until we reach the maximum precision level without 
 *   finding a TernaryBoehmReal which satisfies the predicate, in which case
 *   we return a failure value.
 */
abstract class Search {
    PredicateCode Predicate;
    FunctionCode Function;
    VariableIntervalCode CompactInterval;
    int Epsilon;
    Boolean Found;
    ArrayList<SpecificIntervalCode> Frontier;

    abstract void discretize();
    abstract void sort();
    abstract void refine();
    abstract void insert();
    abstract Boolean check();
    abstract void search();
}

/*
 * The following class implements a naive search strategy. The naive search
 * strategy applies no heuristics, and simply searches the compact interval
 * in the order in which the points appear in the ArrayList. This is the
 * simplest search strategy, and is used as a baseline for comparison with
 * other search strategies.
 * 
 * The predicate code provides a delta value, which is the precision level at
 * which the predicate is to be checked. The function code provides a 
 * uniform continuity oracle, tells us at which precision level the compact
 * interval is to be discretised.
 */
class NaiveSearch extends Search {
    NaiveSearch(PredicateCode P, FunctionCode F, VariableIntervalCode K, int epsilon) {
        Predicate = P;
        Function = F;
        CompactInterval = K;
        Epsilon = epsilon;
        Frontier = new ArrayList<SpecificIntervalCode>();
        Found = false;
    }

    // discretize the compact interval into a finite set of points on the 
    // precision level given by the uniform continuity oracle of the function
    void discretize() {
        SpecificIntervalCode initial = new SpecificIntervalCode(CompactInterval);
        int delta = Function.getUniformContinuityOracle(initial).apply(Epsilon).get(0);
        BigInteger current = initial.downLeft(delta - initial.getPrec()).getCode();
        BigInteger end = initial.downRight(delta - initial.getPrec()).getCode();
        while (current.compareTo(end) < 1) {
            Frontier.add(new SpecificIntervalCode(new DyadicCode(current,delta)));
            current = current.add(BigInteger.TWO);
        }        
    }
  
    // no sorting
    void sort() {
        return;
    }

    // we are using the naive search strategy which searches the compact interval
    // at the given precision level in the order in which the points appear in
    // the ArrayList, so we simply remove the first element of the ArrayList
    void refine() {
        Frontier.remove(0);
    }

    // no insertion
    void insert() {
        return;
    }

    // check whether predicate is satisfied
    Boolean check() {
        TernaryBoehmReal x = new TernaryBoehmReal(Frontier.get(0).getLeftEndpoint());
        return Predicate.apply(x);
    }

    // run the search algorithm, and set Found to true if a solution is found
    void search() {
        discretize();
        sort();
        while (!Frontier.isEmpty() && !Found) {
            if (check()) {
                Found = true;
                break;
            } else {
                refine();
                insert();
            }
        }
    }

    public String toString() {
        if (Found) {
            return "Predicate satisfied";
        } else {
            return "Predicate not satisfied";
        }
    }

    public TernaryBoehmReal getReal() {
        if (Found) {
            SpecificIntervalCode interval = Frontier.get(0);
            return new TernaryBoehmReal(interval.getLeftEndpoint());
        } else {
            throw new RuntimeException("Predicate not satisfied");
        }
    }

    public String toInterval(Integer precision) {
        if (Found) {
            TernaryBoehmReal solution = getReal();
            return solution.toString(precision);
        } else {
            throw new RuntimeException("Predicate not satisfied");
        }
    }

    public Double toDouble(Integer precision) {
        if (Found) {
            TernaryBoehmReal solution = getReal();
            return solution.toDouble(precision);
        } else {
            throw new RuntimeException("Predicate not satisfied");
        }
    }

}

/*
 * The following is an instance of Naive search which checks if the function
 * contains a value which is greater than or equal to a given value.
 */
class NaiveSearchGeq extends NaiveSearch {
    NaiveSearchGeq(TernaryBoehmReal x , FunctionCode F, VariableIntervalCode K, int epsilon) {
        super(PredicateCode.geq(x, epsilon), F, K, epsilon);
    }
}

/*
 * A potential improvement to the naive search strategy is to randomly shuffle
 * the ArrayList at the beginning of the search algorithm. This is implemented
 * in the following class.
 */
class RandomSearch extends NaiveSearch {
    RandomSearch(PredicateCode P, FunctionCode F, VariableIntervalCode K, int epsilon) {
        super(P, F, K, epsilon);
    }

    // shuffle the ArrayList
    void sort() {
        Collections.shuffle(Frontier);
    }
}

/*
 * Another simple technique for improving the naive search strategy is to
 * shuffle the ArrayList at each iteration of the search algorithm. This is
 * implemented in the following class.
 */
class ShuffleSearch extends NaiveSearch {
    ShuffleSearch(PredicateCode P, FunctionCode F, VariableIntervalCode K, int epsilon) {
        super(P, F, K, epsilon);
    }

    // shuffle the ArrayList at each iteration
    void refine() {
        Frontier.remove(0);
        Collections.shuffle(Frontier);      
    }
}

/*
 * In order to compare the performance of the naive search strategy with the
 * performance of the random search strategy, we implement the following class
 * which runs both search strategies and returns the time taken by each.
 * In particular, we compare the time taken by each search strategy to find
 * a solution to the Geq predicate.
 */
class CompareSearches {
    static void compareNano(PredicateCode P, FunctionCode F, VariableIntervalCode K, int epsilon) {
        long startTime = System.nanoTime();
        NaiveSearch naive = new NaiveSearch(P, F, K, epsilon);
        naive.search();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Naive search took " + duration + " nanoseconds");
        startTime = System.nanoTime();
        RandomSearch random = new RandomSearch(P, F, K, epsilon);
        random.search();
        endTime = System.nanoTime();
        duration = (endTime - startTime);
        System.out.println("Random search took " + duration + " nanoseconds");
    }

    static void compareGeq(TernaryBoehmReal x, FunctionCode F, VariableIntervalCode K, int epsilon) {
        PredicateCode P = PredicateCode.geq(x, epsilon);
        compareNano(P, F, K, epsilon);
    }

    static void compareMilli(PredicateCode P, FunctionCode F, VariableIntervalCode K, int epsilon) {
        long startTime = System.nanoTime();
        NaiveSearch naive = new NaiveSearch(P, F, K, epsilon);
        naive.search();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Naive search took " + duration + " milliseconds");
        startTime = System.nanoTime();
        RandomSearch random = new RandomSearch(P, F, K, epsilon);
        random.search();
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        System.out.println("Random search took " + duration + " milliseconds");
    }

    /*
     * The following function is the same as above but returns the solution
     * of each search strategy as a double as well as the time taken.
     */
    static void compareGeqDouble(TernaryBoehmReal x, FunctionCode F, VariableIntervalCode K, int epsilon) {
        PredicateCode P = PredicateCode.geq(x, epsilon);
        long startTime = System.nanoTime();
        NaiveSearch naive = new NaiveSearch(P, F, K, epsilon);
        naive.search();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Naive search took " + duration + " milliseconds");
        startTime = System.nanoTime();
        RandomSearch random = new RandomSearch(P, F, K, epsilon);
        random.search();
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        System.out.println("Random search took " + duration + " milliseconds");
        System.out.println("Naive search solution: " + naive.toDouble(epsilon));
        System.out.println("Random search solution: " + random.toDouble(epsilon));
    }

    /*
     * The following function is the same as above but includes the shuffle
     * search strategy.
     */
    static void compareGeqShuffle(TernaryBoehmReal x, FunctionCode F, VariableIntervalCode K, int epsilon) {
        PredicateCode P = PredicateCode.geq(x, epsilon);
        long startTime = System.nanoTime();
        NaiveSearch naive = new NaiveSearch(P, F, K, epsilon);
        naive.search();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Naive search took " + duration + " milliseconds");
        startTime = System.nanoTime();
        RandomSearch random = new RandomSearch(P, F, K, epsilon);
        random.search();
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        System.out.println("Random search took " + duration + " milliseconds");
        startTime = System.nanoTime();
        ShuffleSearch shuffle = new ShuffleSearch(P, F, K, epsilon);
        shuffle.search();
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        System.out.println("Shuffle search took " + duration + " milliseconds");
        System.out.println("Naive search solution: " + naive.toDouble(epsilon));
        System.out.println("Random search solution: " + random.toDouble(epsilon));
        System.out.println("Shuffle search solution: " + shuffle.toDouble(epsilon));
    }

    static void compareGeqMilli(TernaryBoehmReal x, FunctionCode F, VariableIntervalCode K, int epsilon) {
        PredicateCode P = PredicateCode.geq(x, epsilon);
        compareMilli(P, F, K, epsilon);
    }

    /*
     * The following function compares the performance of the naive search
     * strategy with the performance of the random search strategy on the
     * function x^2 + 1, on the compact interval [0,2], to
     * precision level 12. 
     * 
     * Running this function shows that the random search strategy is faster
     * by roughly an order of magnitude.
     */
    static void compareGeqExample() {
        FunctionCode F = FunctionCode.unaryPolynomial(Arrays.asList(
            new Pair<>(new TernaryBoehmReal(1),2),
            new Pair<>(new TernaryBoehmReal(1),0)
        ));

        VariableIntervalCode K = new VariableIntervalCode(BigInteger.valueOf(0), BigInteger.valueOf(2), 0);
        compareGeqShuffle(new TernaryBoehmReal(1), F, K, 13);
    }

}

/*
 * One of the major applications of searching algorithms is finding the 
 * minimum value of a function in a given interval. Finding the root of a 
 * function is a special case of this problem. The following search algorithm
 * is a naive implementation of this problem.
 * 
 * Since we are minimising the function, we do not have an initial predicate.
 * Instead, we have a function code and a compact interval. We construct an 
 * initial predicate code by applying the function code to a value in the
 * compact interval. Whenever we find a value in the compact interval that
 * satisfies the predicate, we then construct a new predicate code. 
 */
class NaiveSearchMin extends NaiveSearch {
    TernaryBoehmReal currentMin = new TernaryBoehmReal(Integer.MAX_VALUE);

    NaiveSearchMin(FunctionCode F, VariableIntervalCode K, int epsilon) {
        super(PredicateCode.leq(new TernaryBoehmReal(Integer.MAX_VALUE),0) , F, K, epsilon); // uses dummy predicate       
    }

    Boolean check() {
        TernaryBoehmReal x = new TernaryBoehmReal(Frontier.get(0).getLeftEndpoint());
        if (Predicate.apply(x)) {
            currentMin = x;
            return true;
        } else {
            return false;
        }
    }

    void search(){
        // discretize();
        // sort();
        // TernaryBoehmReal currentInput = new TernaryBoehmReal(Frontier.get(0).getLeftEndpoint());
        // TernaryBoehmReal output = 
        // PredicateCode p = PredicateCode.leq(new TernaryBoehmReal(Frontier.get(0).getLeftEndpoint()), Epsilon);
        // while (!Frontier.isEmpty()) {
        //     if (check()) {
        //         p = PredicateCode.leq(new TernaryBoehmReal(Frontier.get(0).getLeftEndpoint()), Epsilon);
        //         refine();
        //         insert();
        //         Found = false;
        //     } else {
        //         refine();
        //         insert();
        //     }
        // }

    }

    public String toString() {
        search();
        return currentMin.toString();
    }

    public TernaryBoehmReal getReal() {
        search();
        return currentMin;
    }

    public String toInterval(Integer precision) {
        search();
        return currentMin.toString(precision);
    }

    public Double toDouble(Integer precision) {
        search();
        return currentMin.toDouble(precision);
    }

}

/*
 * To exemplify the use of the NaiveSearchMin class, we implement the following
 * function which finds the minimum value of - x in the interval [-2,2] to 
 * precision level 10. The minimum value is at 2.
 * 
 * Also given is the search of (x^2 - 1) in the interval [-2,2] to precision 
 * level 10. The minimum value is at 0.
 */
class MinExample {
    static void example() {
        FunctionCode F = FunctionCode.unaryPolynomial(Arrays.asList(
            new Pair<>(new TernaryBoehmReal(-1),1)
        ));
        VariableIntervalCode K = new VariableIntervalCode(BigInteger.valueOf(-2), BigInteger.valueOf(2), 0);
        NaiveSearchMin ns = new NaiveSearchMin(F, K, 10);
        System.out.println(ns.toInterval(10));

    }

    static void example2() {
        FunctionCode F = FunctionCode.unaryPolynomial(Arrays.asList(
            new Pair<>(new TernaryBoehmReal(1),2),
            new Pair<>(new TernaryBoehmReal(-1),0)
        ));
        VariableIntervalCode K = new VariableIntervalCode(BigInteger.valueOf(-2), BigInteger.valueOf(2), 0);
        NaiveSearchMin ns = new NaiveSearchMin(F, K, 20);
        System.out.println(ns.toInterval(10));

    }
}