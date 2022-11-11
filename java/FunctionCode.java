package PLDI;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FunctionCode {

	private int arity;
	private Function<List<VariableIntervalCode>,VariableIntervalCode> approximator;
	private BiFunction<List<TernaryBoehmReal>,Integer,List<Integer>> continuityOracle;
	
	// Constructors
	
	private FunctionCode(int arity,
			Function<List<VariableIntervalCode>,VariableIntervalCode> approximator,
			BiFunction<List<TernaryBoehmReal>,Integer,List<Integer>> continuityOracle) {
		this.arity = arity;
		this.approximator = approximator;
		this.continuityOracle = continuityOracle;
	}
	
	// Getters
	
	public int getArity() {
		return arity;
	}
	
	public Function<List<VariableIntervalCode>,VariableIntervalCode> getApproximator() {
		return approximator;
	}
	
	public BiFunction<List<TernaryBoehmReal>,Integer,List<Integer>> getContinuityOracle() {
		return continuityOracle;
	}
	
	public Function<Integer,List<Integer>> getUniformContinuityOracle(SpecificIntervalCode ki) {
		Function<Integer,List<Integer>> left = n -> continuityOracle.apply(Arrays.asList(new TernaryBoehmReal(ki.downLeft(n - ki.getPrec()).getLeftEndpoint())), n);
		Function<Integer,List<Integer>> right = n -> continuityOracle.apply(Arrays.asList(new TernaryBoehmReal(ki.downRight(n - ki.getPrec()).getLeftEndpoint())), n);
		return (n -> zipMax(Arrays.asList(left.apply(n),right.apply(n))));
	}
	
	public Function<TernaryBoehmReal,TernaryBoehmReal> getDerivative(TernaryBoehmReal epsilon) {
		assert(arity == 1);
		return x -> (F_star(Arrays.asList(x.add(epsilon))).subtract(F_star(Arrays.asList(x)))).divide(epsilon);
	}
	
	// Appliers
	
	public VariableIntervalCode apply(List<VariableIntervalCode> args) {
		return approximator.apply(args);
	}
	
	// Compose functions
	
	private static List<VariableIntervalCode> map(List<FunctionCode> gs, List<VariableIntervalCode> args) {
		return gs.stream().map(g -> g.apply(args)).toList();
	}
	
	private static List<Integer> zipMax(List<List<Integer>> ns) {
		List<Integer> ps = new ArrayList<>();
		for (int i = 0; i < ns.get(0).size(); i++) {
			int p = ns.get(0).get(i);
			for (int j = 1; j < ns.size(); j++) {
				p = Math.max(p, ns.get(j).get(i));
			}
			ps.add(p);
		}
		return ps;
	}
	
	// xs -> f(g1(xs),...,gj(xs))
	public static FunctionCode compose(int arity, FunctionCode f, List<FunctionCode> gs) {
		return new FunctionCode(arity, xs -> f.apply(map(gs,xs)), 
				(xs,q) -> {
					List<TernaryBoehmReal> gxs = gs.stream().map(g -> g.F_star(xs)).toList();
					List<Integer> fps = f.continuityOracle.apply(gxs, q);
					List<List<Integer>> gps = new ArrayList<>();
					for (int i = 0; i < f.arity; i++) {
						gps.add(gs.get(i).continuityOracle.apply(xs, fps.get(i)));
					}
					return zipMax(gps);
				}
			);
	}
	
	// Static members
	
	public static FunctionCode proj(int arity, int i) {
		return new FunctionCode(arity, (xs -> xs.get(i)), 
				(xs,q) -> {
					List<Integer> qs = new ArrayList<>();
					for (int j = 0; j < arity; j++) {
						if (i != j) {
							qs.add(0);
						} else {
							qs.add(q);
						}
					}
					return qs;
				}
			);
	}
	
	public static FunctionCode constant(int arity, TernaryBoehmReal y) {
		return new FunctionCode(arity, (xs -> new SpecificIntervalCode(y, xs.get(0).getPrec()).getVariableIntervalCode()),
				(xs,q) -> {
					List<Integer> qs = new ArrayList<>();
					for (int j = 0; j < arity; j++) {
							qs.add(0);
					}
					return qs;
				}
			);
	}
	
	public static FunctionCode abs() {
		return new FunctionCode(1, xs -> xs.get(0).abs(), (xs,q) -> Arrays.asList(q));
	}
	
	public static FunctionCode negate() {
		return new FunctionCode(1, xs -> xs.get(0).negate(), (xs,q) -> Arrays.asList(q));
	}
	
	public static FunctionCode add() {
		return new FunctionCode(2, xs -> xs.get(0).add(xs.get(1)), (xs,q) -> Arrays.asList(q + 1, q + 1));
	}
	
	public static FunctionCode multiply() {
		return new FunctionCode(2, xs -> xs.get(0).multiply(xs.get(1)), 
				(xs,q) -> {
					
					int p = q + (xs.get(0).approx(q).abs().add(xs.get(1).approx(q).abs()).add(BigInteger.ONE).bitLength()) / 2;
					int p2 = xs.get(0).approx(q).multiply(BigInteger.TWO).abs().add(
								xs.get(1).approx(q).multiply(BigInteger.TWO).abs()
							).bitLength() - 2;
					System.out.println(p + " , " + p2);
					return Arrays.asList(p,p);
				}
			);
	}
	
	public static FunctionCode inverse() {
		return new FunctionCode(1, xs -> {
				BigInteger left = xs.get(0).getLeftCode();
				BigInteger right = xs.get(0).getRightCode();
				int prec = xs.get(0).getPrec();
				if (left.equals(BigInteger.ZERO) || right.equals(BigInteger.ZERO)) {
					return new VariableIntervalCode(BigInteger.ZERO, BigInteger.ZERO, prec);
				}
				BigInteger fourPowP = BigInteger.valueOf(4).pow(xs.get(0).getPrec());
				return new VariableIntervalCode(fourPowP.divide(right), fourPowP.divide(left), prec);			
			},(xs,q) -> Arrays.asList(q)
		);		
	}
	
	public static FunctionCode constantMul(TernaryBoehmReal y) {
		return new FunctionCode(1, xs -> xs.get(0).multiply(y.toVariableFunction().apply(xs.get(0).getPrec())), 
				(xs,q) -> {
					int p = xs.get(0).approx(q).multiply(BigInteger.TWO).abs().add(
								y.approx(q).multiply(BigInteger.TWO).abs()
							).bitLength() - 2;
					return Arrays.asList(p);
				}
			);
	}
	
	// Composed functions
	
	// [x,y] -> +[proj([x,y],0),-proj([x,y],1)] -> +[x,-y] -> x + (- y)
	public static FunctionCode subtract() {
		return compose(2, 
				FunctionCode.add(), 
				Arrays.asList(
						proj(2,0), 
						compose(2, FunctionCode.negate(), Arrays.asList(proj(2,1)))
					)
			);
	}
	
	public static FunctionCode divide() {
		return compose(2, 
				FunctionCode.multiply(), 
				Arrays.asList(
						proj(2,0), 
						compose(2, FunctionCode.inverse(), Arrays.asList(proj(2,1)))
					)
			);
	}
	
	public static FunctionCode pow(int n) {
		if (n <= 0) {
			return constant(1,new TernaryBoehmReal(1));
		} else if (n == 1) {
			return proj(1,0);
		} else {
			return compose(1,FunctionCode.multiply(),Arrays.asList(pow(n/2),pow((n+1)/2)));
		}
	}
	
	public static FunctionCode sum(int arity, List<FunctionCode> xs) {
		if (xs.size() == 1) {
			return xs.get(0);
		} else {
			return compose(arity, FunctionCode.add(), Arrays.asList(sum(arity, xs.subList(0, xs.size()/2)), sum(arity, xs.subList(xs.size()/2, xs.size()))));
		}
	}
	
	// f([x0,...,xarity-1]) = a * xi ^ n
	public static FunctionCode polyTerm(int arity, TernaryBoehmReal a, int i, int n) {
		return compose(arity, constantMul(a), Arrays.asList(compose(arity, pow(n), Arrays.asList(proj(arity,i)))));
	}
	
	public static FunctionCode polynomial(int arity, List<Pair<TernaryBoehmReal,Pair<Integer,Integer>>> ains) {
		return sum(arity, ains.stream().map(ain -> polyTerm(arity, ain.getFst(), ain.getSnd().getFst(), ain.getSnd().getSnd())).toList());
	}
	
	public static FunctionCode unaryPolynomial(List<Pair<TernaryBoehmReal,Integer>> ains) {
		return sum(1, ains.stream().map(ain -> polyTerm(1, ain.getFst(), 0, ain.getSnd())).toList());
	}
	
	// Convergent streams
	
	public Function<Integer,VariableIntervalCode> F_prime(List<Function<Integer,VariableIntervalCode>> args,
														  Function<Integer,List<Integer>> k) {
		return (n) -> {
			List<VariableIntervalCode> args_k = new ArrayList<>();
			for (int i = 0; i < arity; i++) {
				args_k.add(args.get(i).apply(k.apply(n).get(i)));
			}
			return apply(args_k);
		};
	}
	
	public static Function<Integer,SpecificIntervalCode> join(Function<Integer,VariableIntervalCode> x) {
		return (n -> x.apply(n).join_prime());
	}
	
	public TernaryBoehmReal F_star(List<TernaryBoehmReal> args) {
		return new TernaryBoehmReal(join(F_prime(args.stream().map(arg -> arg.toVariableFunction()).toList(),
						((n) -> {
							System.out.println(continuityOracle.apply(args, n));
							return continuityOracle.apply(args, n);
						}))));
	}
	
	public TernaryBoehmReal F_cont(List<TernaryBoehmReal> args, SpecificIntervalCode ki) {
		return new TernaryBoehmReal(join(F_prime(args.stream().map(arg -> arg.toVariableFunction()).toList(),
						((n) -> {
							System.out.println(getUniformContinuityOracle(ki).apply(n));
							return getUniformContinuityOracle(ki).apply(n);
						}))));
	}
	
	
	
}
