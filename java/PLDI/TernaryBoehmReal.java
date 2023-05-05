package PLDI;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.Function;

public class TernaryBoehmReal {

	private Function<Integer,BigInteger> approx;
	
	// Constructors
	
	public TernaryBoehmReal(DyadicCode x) {
		this.approx = (prec) -> {
			int p = x.getDen();
			if (prec == p) {
				return x.getNum();
			} else if (prec < p) {
				return x.upRight(p - prec).getNum();
			} else {
				return x.downLeft(prec - p).getNum();
			}
		};
	}
	
	public TernaryBoehmReal(int n) {
		DyadicCode x = new DyadicCode(n);
		this.approx = (prec) -> {
			int p = x.getDen();
			if (prec == p) {
				return x.getNum();
			} else if (prec < p) {
				return x.upRight(p - prec).getNum();
			} else {
				return x.downLeft(prec - p).getNum();
			}
		};
	}
	
	public TernaryBoehmReal(Function<Integer,SpecificIntervalCode> x) {
		this.approx = (prec) -> {
			DyadicCode kp = x.apply(prec).getLeftEndpoint();
			return kp.upRight(kp.getDen() - prec).getNum();
		};
	}
	
	public TernaryBoehmReal(SpecificIntervalCode s) {
		this.approx = (prec) -> {
			DyadicCode x = s.getLeftEndpoint();
			int p = x.getDen();
			if (prec == p) {
				return x.getNum();
			} else if (prec < p) {
				return x.upRight(p - prec).getNum();
			} else {
				return x.downLeft(prec - p).getNum();
			}
		};
	}
	
	// Getters
	
	public BigInteger approx(int prec) {
		return approx.apply(prec);
	}
	
	public SpecificIntervalCode approxAsSpecificIntervalCode(int prec) {
		return new SpecificIntervalCode(approx(prec), prec);
	}
	
	// To GBR
	
	public Function<Integer,SpecificIntervalCode> toSpecificFunction() {
		return (n -> new SpecificIntervalCode(approx(n), n));
	}
	
	public Function<Integer,VariableIntervalCode> toVariableFunction() {
		return (n -> toSpecificFunction().apply(n).getVariableIntervalCode());
	}
	
	// To Dyadics
	
	private DyadicCode lower(int n) {
		return new DyadicCode(approx(n), n);
	}
	
	private DyadicCode upper(int n) {
		return lower(n).next();
	}
	
	// Functions
	
	public TernaryBoehmReal abs() {
		return FunctionCode.abs().F_star(Arrays.asList(this));
	}
	
	public TernaryBoehmReal negate() {
		return FunctionCode.negate().F_star(Arrays.asList(this));
	}
	
	public TernaryBoehmReal add(TernaryBoehmReal y) {
		return FunctionCode.add().F_star(Arrays.asList(this, y));
	}
	
	public TernaryBoehmReal subtract(TernaryBoehmReal y) {
		return FunctionCode.subtract().F_star(Arrays.asList(this, y));
	}
	
	public TernaryBoehmReal multiply(TernaryBoehmReal y) {
		return FunctionCode.multiply().F_star(Arrays.asList(this, y));
	}
	
	public TernaryBoehmReal inverse() {
		return new TernaryBoehmReal((prec) -> {
			int prec_ = prec + 1;
			BigInteger left = approx(prec_);
			BigInteger right = approx(prec_).add(BigInteger.TWO);
			while (left == BigInteger.ZERO || right == BigInteger.ZERO) {
				prec_++;
				left = approx(prec_);
				right = approx(prec_).add(BigInteger.TWO);
			}
			BigInteger fourPowP = BigInteger.valueOf(4).pow(prec_);
			return new SpecificIntervalCode(fourPowP.divide(left), prec_);
		});
	}
	
	public TernaryBoehmReal divide(TernaryBoehmReal y) {
		return multiply(y.inverse());
	}
	
	
	
	// Printers
	
	public String toString(int n) {
		return "[" + lower(n).toString() + "," + upper(n).toDouble() + "]"; 
	}
	
	public double toDouble(int n) {
		return lower(n).toDouble();
	}
	
	public String toString() {
		String str = "";
		for (int i = 0; i <= 20; i++) {
			str += toString(i) + "\n";
		}
		return str;
	}

	
	
}
