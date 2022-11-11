package PLDI;

import java.math.BigInteger;
import java.util.Arrays;

public class Examples {
	
	public static TernaryBoehmReal half = new TernaryBoehmReal(new DyadicCode(BigInteger.ONE, 1));
	
	public static SpecificIntervalCode minusOneToOne = new SpecificIntervalCode(new DyadicCode(BigInteger.ONE.negate(), 0));
	
	public static SpecificIntervalCode minusFourToFour = new SpecificIntervalCode(new VariableIntervalCode(BigInteger.valueOf(-4), BigInteger.valueOf(4), 0)); 
	
	public static FunctionCode poly1 = FunctionCode.unaryPolynomial(Arrays.asList(
					new Pair<>(new TernaryBoehmReal(8),10),
					new Pair<>(new TernaryBoehmReal(-6),3),
					new Pair<>(new TernaryBoehmReal(-4),2)
				));
	
	// 0.1 (and on 3N, BB)
	public static TernaryBoehmReal arbitrary_ex1() {
		return new TernaryBoehmReal(1).divide(new TernaryBoehmReal(10));
	}
	
	// 1 000 000 000 * (1/2^50) (and on BB)
	public static TernaryBoehmReal arbitrary_ex2() {
		return new TernaryBoehmReal(1000000000).multiply(new TernaryBoehmReal(new DyadicCode(BigInteger.ONE, 50)));
	}
	
	// f(-10) (and on BB)
	public static TernaryBoehmReal arbitrary_ex3() {
		return poly1.F_star(Arrays.asList(new TernaryBoehmReal(-10)));
	}
	
	// f(0) (and on BB)
	public static TernaryBoehmReal arbitrary_ex4() {
		return poly1.F_star(Arrays.asList(new TernaryBoehmReal(0)));
	}
	
	// f(99.5) (and on BB)
	public static TernaryBoehmReal arbitrary_ex5() {
		return poly1.F_star(Arrays.asList(new TernaryBoehmReal(new DyadicCode(BigInteger.valueOf(199),1))));
	}
	
	public static TernaryBoehmReal find_derivative(FunctionCode f, TernaryBoehmReal x, int prec) {
		return poly1.getDerivative(new TernaryBoehmReal(new DyadicCode(BigInteger.ONE, prec))).apply(x);
	}
	
	// Find x in compact such that f(x) = y
	public static TernaryBoehmReal solve_equation_naive(FunctionCode f, TernaryBoehmReal y, SpecificIntervalCode compact, int prec) {
		assert(f.getArity() == 1);
		return Searchers.exhaustive_search_naive(Searchers.searchPFinCompact(PredicateCode.eq(y, prec),f,compact),compact);
	}
	
	// Find x in compact such that f(x) = y
	public static TernaryBoehmReal solve_equation_semidecidable(FunctionCode f, TernaryBoehmReal y, SpecificIntervalCode compact, int prec) {
		assert(f.getArity() == 1);
		return Searchers.exhaustive_search_semidecidable(
				Searchers.searchPFinCompact(PredicateCode.eq(y, prec), f, compact), 
				compact, 
				(delta,x) -> f.getApproximator().apply(
						Arrays.asList(x.approxAsSpecificIntervalCode(delta).getVariableIntervalCode()))
						.intersectsWith(y)
				);
	}
	
	// Find x in [-1,1] such that x * 0.5 = 0.5 to prec-level 20 (and on 3N)
	public static TernaryBoehmReal equation_ex1() {
		return solve_equation_naive(FunctionCode.constantMul(half), half, minusOneToOne, 20);
	}
	
	// Find x in [-1,1] such that x * 0.5 = 0.5 to prec-level 20 (and on 3N)
	public static TernaryBoehmReal equation_ex2() {
		return solve_equation_semidecidable(FunctionCode.constantMul(half), half, minusOneToOne, 20);
	}
	
	// Find x in [-1,1] such that x * 0.5 = 0.5 to prec-level 500 (and on 3N)
	public static TernaryBoehmReal equation_ex3() {
		return solve_equation_semidecidable(FunctionCode.constantMul(half), half, minusOneToOne, 500);
	}
	
	// Minimise x^2 in [-1,1] to prec-level 50 (and on 3N)
	public static TernaryBoehmReal minimise_ex1() { 
		return Searchers.minimise(FunctionCode.pow(2), minusOneToOne, 50);
	}
	
	// Minimise x^2 in [-1,1] to prec-level 1000 (and on 3N)
	public static TernaryBoehmReal minimise_ex2() {
		return Searchers.minimise(FunctionCode.pow(2), minusOneToOne, 1000);
	}
	
	// Miimise f(x) in [-4,4] to prec-level 20
	public static TernaryBoehmReal minimise_ex3() {
		return Searchers.minimise(poly1, minusFourToFour, 20);
	}

}
