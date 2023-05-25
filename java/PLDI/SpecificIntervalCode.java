package PLDI;

import java.math.BigInteger;
import java.util.ArrayList;

public class SpecificIntervalCode {
	
	// Implementation of type I := Z x Z
	// Only holds interval codes of form (l,l+2,p) in order to represent [l/2^{p},(l+2)/2^{p}]
	
	private static final BigInteger Frontier = null;
	private VariableIntervalCode code;
	
	public SpecificIntervalCode(BigInteger left, int prec) {
		this.code = new VariableIntervalCode(left, left.add(BigInteger.TWO), prec);
	}
	
	public SpecificIntervalCode(DyadicCode dyadic) {
		this.code = new VariableIntervalCode(dyadic.getNum(), dyadic.getNum().add(BigInteger.TWO), dyadic.getDen());
	}
	
	public SpecificIntervalCode(TernaryBoehmReal x, int prec) {
		this.code = new VariableIntervalCode(x.approx(prec), x.approx(prec).add(BigInteger.TWO), prec);
	}
	
	public VariableIntervalCode getVariableIntervalCode() {
		return code;
	}
	
	public SpecificIntervalCode(VariableIntervalCode variable) {
		this.code = variable.join_prime().getVariableIntervalCode();
	}
	
	public DyadicCode getLeftEndpoint() {
		return code.getLeftEndpoint();
	}
	
	public DyadicCode getRightEndpoint() {
		return code.getRightEndpoint();
	}
	
	public BigInteger getCode() {
		return code.getLeftCode();
	}
	
	public int getPrec() {
		return code.getPrec();
	}
	
	public String toString() {
		return code.toString();
	}
	
	public SpecificIntervalCode downLeft() {
		return new SpecificIntervalCode(code.downLeft());
	}
	
	public SpecificIntervalCode downMid() {
		return new SpecificIntervalCode(new DyadicCode(code.getLeftCode().add(BigInteger.ONE), code.getPrec() + 1));
	}
	
	public SpecificIntervalCode downRight() {
		return new SpecificIntervalCode(code.downRight());
	}
	
	public SpecificIntervalCode downLeft(int n) {
		return new SpecificIntervalCode(code.downLeft(n));
	}
	
	public SpecificIntervalCode downRight(int n) {
		return new SpecificIntervalCode(code.downRight(n));
	}
	
	public SpecificIntervalCode upRight() {
		return new SpecificIntervalCode(code.getLeftEndpoint().upRight());
	}
	
	public SpecificIntervalCode upRight(int n) {
		return new SpecificIntervalCode(code.getLeftEndpoint().upRight(n));
	}
	
    /*
     * A specific interval code is less than another specific interval code if
     * the left endpoint of the first is less than the left endpoint of the
     * second.
     */
    public boolean lessThan(SpecificIntervalCode sx, SpecificIntervalCode sy) {
        VariableIntervalCode fx = sx.getVariableIntervalCode();
        VariableIntervalCode fy = sy.getVariableIntervalCode();
        if (fx.getPrec() > fy.getPrec()) {
			fy = fy.down(fx.getPrec() - fy.getPrec());
		} else if (fx.getPrec() < fy.getPrec()) {
			fx = fx.down(fy.getPrec() - fx.getPrec());
		}
		return (fx.getLeftCode().compareTo(fy.getLeftCode()) <= 0);
    }

    /*
     * A variable interval code is less than another variable interval code if
     * the left endpoint of the first is less than the left endpoint of the
     * second.
     */
    public static boolean lessThan(VariableIntervalCode fx, VariableIntervalCode fy) {
        if (fx.getPrec() > fy.getPrec()) {
            fy = fy.down(fx.getPrec() - fy.getPrec());
        } else if (fx.getPrec() < fy.getPrec()) {
            fx = fx.down(fy.getPrec() - fx.getPrec());
        }
        return (fx.getLeftCode().compareTo(fy.getLeftCode()) <= 0);
    }

	public static boolean lessThanRight(VariableIntervalCode fx, VariableIntervalCode fy) {
        if (fx.getPrec() > fy.getPrec()) {
            fy = fy.down(fx.getPrec() - fy.getPrec());
        } else if (fx.getPrec() < fy.getPrec()) {
            fx = fx.down(fy.getPrec() - fx.getPrec());
        }
        return (fx.getRightCode().compareTo(fy.getRightCode()) <= 0);
    }

	//discretize the range [a,b] into 2^Epsilon intervals
    public static ArrayList<SpecificIntervalCode> discretize(int delta, VariableIntervalCode compactInterval) {
		ArrayList<SpecificIntervalCode> frontier = new ArrayList<SpecificIntervalCode>();
        SpecificIntervalCode initial = new SpecificIntervalCode(compactInterval);
        BigInteger current = initial.downLeft(delta - initial.getPrec()).getCode();
        BigInteger end = initial.downRight(delta - initial.getPrec()).getCode();
        while (current.compareTo(end) < 1) {
            frontier.add(new SpecificIntervalCode(new DyadicCode(current,delta)));
            current = current.add(BigInteger.TWO);
        }  

        return frontier;
    }

}
