package PLDI;

import java.math.BigInteger;

public class SpecificIntervalCode {
	
	// Implementation of type I := Z x Z
	// Only holds interval codes of form (l,l+2,p) in order to represent [l/2^{p},(l+2)/2^{p}]
	
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
	
}
