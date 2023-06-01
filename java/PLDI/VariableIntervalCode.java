package PLDI;

import java.math.BigInteger;

public class VariableIntervalCode { 
	
	// Implementation of type I_v := Z x Z x Z
	// (l,r,p) represents interval [l/2^{p},r/2^{p}]
	
	private BigInteger left;
	private BigInteger right;
	private int prec;
	
	public VariableIntervalCode(BigInteger left, BigInteger right, int prec) {
		this.left = left;
		this.right = right;
		this.prec = prec;
	}
	
	public DyadicCode getLeftEndpoint() {
		return new DyadicCode(left,prec);
	}
	
	public DyadicCode getMidpoint() {
		return new DyadicCode(left.add(right).shiftRight(1),prec);
	}
	
	public DyadicCode getRightEndpoint() {
		return new DyadicCode(right,prec);
	}
	
	public SpecificIntervalCode join_prime() {
		return new SpecificIntervalCode(getLeftEndpoint().upRight(right.subtract(left).bitLength() - 2));
	}

	public int getPrec() {
		return prec;
	}

	public BigInteger getLeftCode() {
		return left;
	}
	
	public BigInteger getRightCode() {
		return right;
	}
	
	public VariableIntervalCode negate() { // [a,b] -> [-b,-a]
		return new VariableIntervalCode(right.negate(), left.negate(), prec);
	}
	
	public VariableIntervalCode abs() {
		if (left.compareTo(BigInteger.ZERO) < 0) {
			if (right.compareTo(BigInteger.ZERO) < 0) {
				return this.negate();
			} else {
				return new VariableIntervalCode(BigInteger.ZERO, right, prec);
			}
		} else {
			return this;
		}
	}
	
	public VariableIntervalCode add(VariableIntervalCode y) { // [a,b] + [c,d] -> [a+c,b+d]
		if (prec == y.prec) {
			return new VariableIntervalCode(left.add(y.left), right.add(y.right), prec);
		} else {
			int minDen = Math.min(prec, y.prec);
			BigInteger l = left.shiftLeft(y.prec-minDen).add(y.left.shiftLeft(prec-minDen));
			BigInteger r = right.shiftLeft(y.prec-minDen).add(y.right.shiftLeft(prec-minDen));
			return new VariableIntervalCode(l, r, Math.max(prec,y.prec));
		}
	}
	
	public VariableIntervalCode multiply(VariableIntervalCode y) { // [a,b] * [c,d] = [min(ac,ad,bc,bd),max(ac,ad,bc,bd)]
		BigInteger a = left.multiply(y.left);
		BigInteger b = left.multiply(y.right);
		BigInteger c = right.multiply(y.left);
		BigInteger d = right.multiply(y.right);
		BigInteger l = a.min(b).min(c).min(d);
		BigInteger r = a.max(b).max(c).max(d);
		return new VariableIntervalCode(l, r, prec + y.prec);
	}
	
	public String toString() {
		return "(" + left + "," + right + "," + prec + ")";
	}
	
	public VariableIntervalCode down() {
		return down(1);
	}
	
	public VariableIntervalCode down(int n) {
		return new VariableIntervalCode(getLeftCode().shiftLeft(n), getRightCode().shiftLeft(n), prec + n);
	}
	
	
	public VariableIntervalCode downLeft() {
		BigInteger d = right.subtract(left);
		return new VariableIntervalCode(left.multiply(BigInteger.TWO), right.multiply(BigInteger.TWO).subtract(d), prec + 1);
	}
	
	public VariableIntervalCode downRight() {
		BigInteger d = right.subtract(left);
		return new VariableIntervalCode(left.multiply(BigInteger.TWO).add(d), right.multiply(BigInteger.TWO), prec + 1);
	}
	
	public VariableIntervalCode downLeft(int n) {
		if (n <= 0) {
			return this;
		} else {
			return downLeft().downLeft(n-1);
		}
	}
	
	public VariableIntervalCode downRight(int n) {
		if (n <= 0) {
			return this;
		} else {
			return downRight().downRight(n-1);
		}
	}
	
	public boolean intersectsWith(TernaryBoehmReal y) {
		BigInteger yleft = y.approxAsSpecificIntervalCode(prec).getVariableIntervalCode().getLeftCode();
		BigInteger yright = y.approxAsSpecificIntervalCode(prec).getVariableIntervalCode().getRightCode();
		return !(right.compareTo(yleft) < 0 || yright.compareTo(left) < 0);
	}

	 /*
     * A variable interval code is eclipsed by another variable interval code if
     * the right endpoint of the first is less than the left endpoint of the
     * second.
     */
    public static boolean eclipses(VariableIntervalCode fx, VariableIntervalCode fy) {
		if (fx.getPrec() > fy.getPrec()) {
			fy = fy.down(fx.getPrec() - fy.getPrec());
		} else if (fx.getPrec() < fy.getPrec()) {
			fx = fx.down(fy.getPrec() - fx.getPrec());
		}
		return (fx.getRightCode().compareTo(fy.getLeftCode()) <= 0);
	}
}
