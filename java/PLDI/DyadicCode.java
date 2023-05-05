package PLDI;

import java.math.BigInteger;

public class DyadicCode {
	
	private BigInteger num;
	private int den;
	
	// Constructors
	
	public DyadicCode(BigInteger n, int d) {
		this.num = n;
		this.den = d;
	}
	
	public DyadicCode(int n, int d) {
		this.num = BigInteger.valueOf(n);
		this.den = d;
	}
	
	public DyadicCode(int n) {
		this.num = BigInteger.valueOf(n);
		this.den = 0;
	}
	
	// Getters and setters
	
	public BigInteger getNum() {
		return num;
	}
	
	public void setNum(int n) {
		this.num = BigInteger.valueOf(n);
	}
	
	public int getDen() {
		return den;
	}
	
	// Structural
	
	public DyadicCode downLeft() {
		return new DyadicCode(num.shiftLeft(1), den + 1);
	}
	
	public DyadicCode downLeft(int n) {
		if (n <= 0) {
			return this;
		} else {
			return new DyadicCode(num.shiftLeft(n), den + n);
		}
	}
	
	public DyadicCode downRight() {
		return downLeft().next();
	}
	
	public DyadicCode downRight(int n) {
		if (n <= 0) {
			return this;
		} else {
			return next().downLeft(n).prev();
		}
	}
	
	public DyadicCode upRight() {
		if (isIntermediary() && isNegative()) {
			return new DyadicCode(num.divide(BigInteger.TWO).subtract(BigInteger.ONE), den - 1);
		} else {
			return new DyadicCode(num.divide(BigInteger.TWO), den - 1);
		}
	}
	
	public DyadicCode upRight(int n) {
		if (n <= 0) {
			return this;
		} else {
			return upRight().upRight(n-1);
		}
	}
	
	public DyadicCode next() {
		return new DyadicCode(num.add(BigInteger.TWO), den);
	}
	
	public DyadicCode prev() {
		return new DyadicCode(num.subtract(BigInteger.TWO), den);
	}
	
	// Helpers
	
	public boolean isIntermediary() {
		return (num.divide(BigInteger.TWO) == BigInteger.ONE);
	}
	
	public boolean isNegative() {
		return (num.compareTo(BigInteger.ZERO) == -1);
	}
	
	// Arithmetic
	
	public DyadicCode abs() {
		if (isNegative()) {
			return negate();
		} else {
			return this;
		}
	}
	
	public DyadicCode negate() {
		return new DyadicCode(num.negate(), den);
	}

	public DyadicCode add(DyadicCode y) {
		if (den == y.den) {
			return new DyadicCode(num.add(y.num), den);
		} else {
			int minDen = Math.min(den, y.den);
			BigInteger addNum = num.shiftLeft(y.den-minDen).add(y.num.shiftLeft(den-minDen));
			return new DyadicCode(addNum, Math.max(den,y.den));
		}
	}
	
	public DyadicCode subtract(DyadicCode y) {
		return add(y.negate());
	}
	
	public DyadicCode multiply(DyadicCode y) {
		return new DyadicCode(num.multiply(y.num), den+y.den);
	}
	
	// Minimum and maximum
	
	public int compare(DyadicCode y) {
		if (den == y.den) {
			return num.compareTo(y.num);
		} else {
			int maxDen = Math.max(den, y.den);
			return downLeft(maxDen - den).compare(y.downLeft(maxDen - y.den));
		}
	}
	
	public DyadicCode min(DyadicCode y) {
		if (compare(y) == -1) { // i.e. this < y
			return this;
		} else {
			return y;
		}
	}
	
	public DyadicCode max(DyadicCode y) {
		if (compare(y) == 1) { // i.e. this > y
			return this;
		} else {
			return y;
		}
	}
	
	// Printers
	
	public double toDouble() {
		return num.doubleValue() / Math.pow(2, den);
	}
	
	public String toString() {
		return "(" + num + "," + den + ") = " + toDouble();
	}

}
