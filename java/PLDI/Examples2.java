package PLDI;

import java.math.BigInteger;
import java.util.Arrays;

public class Examples2 {

    // Reals
    TernaryBoehmReal one = new TernaryBoehmReal(1);
    TernaryBoehmReal half = new TernaryBoehmReal(new DyadicCode(BigInteger.ONE, 1));

    // Intervals

    // [0,2]
    VariableIntervalCode zero_two = new VariableIntervalCode(BigInteger.valueOf(0), BigInteger.valueOf(2), 0);
    // [-1,1]
    VariableIntervalCode minus_one_one = new VariableIntervalCode(BigInteger.valueOf(-10), BigInteger.valueOf(10), 0);
    // [-2,2]
    VariableIntervalCode minus_two_two = new VariableIntervalCode(BigInteger.valueOf(-2), BigInteger.valueOf(2), 0);
    // [-4,4]
    VariableIntervalCode minus_four_four = new VariableIntervalCode(BigInteger.valueOf(-4), BigInteger.valueOf(4), 0);
     
    // Predicates
 
    // >= 1 on level 10
    PredicateCode pred1 = PredicateCode.geq(one, 10);
    // =1 on level 6
    PredicateCode pred2 = PredicateCode.eq(one, 6);

    // Functions

    // x^2 + 1
    FunctionCode poly1 = FunctionCode.unaryPolynomial(
        Arrays.asList(
            new Pair<>(new TernaryBoehmReal(1),2),
            new Pair<>(new TernaryBoehmReal(1),0)
        )
    );
    // x^6 - x^4 + x^3 + x^2
    public FunctionCode poly2 = FunctionCode.unaryPolynomial(
        Arrays.asList(
	        new Pair<>(new TernaryBoehmReal(1),6),
		    new Pair<>(new TernaryBoehmReal(-1),4),
		    new Pair<>(new TernaryBoehmReal(1),3),
		    new Pair<>(new TernaryBoehmReal(1),2)
        )
    );
    // x^6 + x^5 - x^4 + x^2
    public FunctionCode poly3 = FunctionCode.unaryPolynomial(
        Arrays.asList(
            new Pair<>(new TernaryBoehmReal(1),6),
            new Pair<>(new TernaryBoehmReal(1),5),
            new Pair<>(new TernaryBoehmReal(-1),4),
            new Pair<>(new TernaryBoehmReal(1),2)
        )
    );

    /*
     * Search x^2 + 1 for x such that x^2 + 1 >= 1, up to precision level 10 
     * (correct up to 2^-10, or 1/1024).
     * 
     * Prints the following:
     * - The number of intervals checked
     * - The time taken to do the search
     * - The real found
     */
    void search_example_base_geq() {
        System.out.println("Searching for x such that x^2 + 1 >= 1, up to precision level 10...");
        FunctionSearch search = new FunctionSearch(poly1, pred1, zero_two);
        search.search_verbose();
    }

    /*
     * Same as search_example, but using the random search heuristic.
     */
    void search_example_random_geq() {
        System.out.println("Searching for x such that x^2 + 1 >= 1, up to precision level 10...");
        SearchHeuristic search = new SearchRandom(poly1, pred1, zero_two);
        search.search_verbose();

    }

    /*
     * Search the x^6 - x^4 + x^3 + x^2 for x such that 
     * x^6 - x^4 + x^3 + x^2 = 2, up to precision level 6 on the range [-1,1].
     */
    void search_example_base_eq() {
        System.out.println("Searching for x such that x^6 - x^4 + x^3 + x^2 = 2, up to precision level 6");
        FunctionSearch search = new FunctionSearch(poly2, pred2, minus_one_one);
        search.search_verbose();
    }

    /*
     * Same as search_example, but using the random search heuristic.
     */

    void search_example_random_eq() {
        System.out.println("Searching for x such that x^6 - x^4 + x^3 + x^2 = 2, up to precision level 6");
        SearchHeuristic search = new SearchRandom(poly2, pred2, minus_one_one);
        search.search_verbose();
    }

    /*
     * Minimise x^2 + 1 on [-1,1] up to precision level 12.
     * Obviously the answer is 0, with output 1.
     */
    void minimise_example_base() {
        System.out.println("Minimise x^2 + 1 on [-1,1]...");
        Optimisation2 optimisation = new Optimisation2(poly1, zero_two , 12);
        optimisation.minimise_verbose();
    }

    void minimise_example_base_all_answers() {
        System.out.println("Minimise x^2 + 1 on [-1,1]...");
        Optimisation2 optimisation = new Optimisation2(poly1, minus_one_one , 12);
        optimisation.minimise_verbose_number_answers();
    }

    /*
     * Same as minimise_example_base, but use lower precision levels to guide
     * the search.
     */
    void minimise_example_guided() {
        System.out.println("Minimise x^2 + 1 on [-1,1]...");
        minimisation_heuristic heuristic = new minimisation_heuristic(poly1, minus_one_one, 12);
        heuristic.minimise_verbose();
    }

    /*
     * Same as minimise_example_guided, but use the random search heuristic.
     */
    void minimise_example_random() {
        System.out.println("Minimise x^2 + 1 on [-1,1]...");
        minimisation_heuristic_random heuristic = new minimisation_heuristic_random(poly1, minus_one_one,12);
        heuristic.minimise_verbose();
    }

    void minimise_example_random_all_answers() {
        System.out.println("Minimise x^2 + 1 on [-1,1]...");
        minimisation_heuristic_random heuristic = new minimisation_heuristic_random(poly1, minus_one_one,12);
        heuristic.minimise_verbose_number_answers();
    }

    void minimise_example_base_2() {
        System.out.println("Minimise x^6 - x^4 + x^3 + x^2 on [-1,1]...");
        Optimisation2 optimisation = new Optimisation2(poly2, minus_one_one , 5);
        optimisation.minimise_verbose();
    }

    void minimise_example_random_2() {
        System.out.println("Minimise x^6 - x^4 + x^3 + x^2 on [-1,1]...");
        minimisation_heuristic_random heuristic = new minimisation_heuristic_random(poly2, minus_four_four , 10);
        heuristic.minimise_verbose_number_answers();
    }

    void minimise_example_base_3() {
        System.out.println("Minimise x^6 + x^5 - x^4 + x^2 on [-2,2]...");
        Optimisation2 optimisation = new Optimisation2(poly3, minus_two_two , 5);
        optimisation.minimise_verbose();
    }

    void minimise_example_random_3() {
        System.out.println("Minimise x^6 + x^5 - x^4 + x^2 on [-2,2]...");
        minimisation_heuristic_random heuristic = new minimisation_heuristic_random(poly3, minus_two_two , 10);
        heuristic.minimise_verbose_number_answers();
    }
}
