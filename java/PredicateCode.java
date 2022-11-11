package PLDI;

import java.util.function.Function;

public class PredicateCode {
	
	private Function<TernaryBoehmReal,Boolean> predicate;
	public int delta;
	
	public PredicateCode(Function<TernaryBoehmReal,Boolean> predicate, int delta) {
		this.predicate = predicate;
		this.delta = delta;
	}
	
	public Function<TernaryBoehmReal,Boolean> getPredicate() {
		return predicate;
	}
	
	public boolean apply(TernaryBoehmReal x) {
		return predicate.apply(x);
	}
	
	public static PredicateCode eq(TernaryBoehmReal y, int epsilon) {
		return new PredicateCode(x -> x.approxAsSpecificIntervalCode(epsilon).getVariableIntervalCode().intersectsWith(y), epsilon);
	}
	
	public static PredicateCode geq(TernaryBoehmReal y, int epsilon) {
		return new PredicateCode(x -> x.approx(epsilon).compareTo(y.approx(epsilon)) >= 0, epsilon);
	}
	
	public static PredicateCode leq(TernaryBoehmReal y, int epsilon) {
		return new PredicateCode(x -> x.approx(epsilon).compareTo(y.approx(epsilon)) <= 0, epsilon);
	}
	
	public PredicateCode not() {
		return new PredicateCode(x -> !predicate.apply(x), delta);
	}
	
	public PredicateCode and(PredicateCode P) {
		return new PredicateCode(x -> predicate.apply(x) && P.predicate.apply(x), 
								 Integer.max(delta, P.delta));
	}
	
	public PredicateCode or(PredicateCode P) {
		return new PredicateCode(x -> predicate.apply(x) || P.predicate.apply(x), 
								 Integer.max(delta, P.delta));
	}
	
}
