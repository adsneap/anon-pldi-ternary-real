package PLDI;

public class ExamplesBoehm {
    
	public static CR pow(CR x, int n) {
		if (n <= 0) {
			return CR.one;
		} else if (n == 1) {
			return x;
		} else {
			return pow(x,((n+1)/2)).multiply(pow(x,(n/2)));
		}
	}

    public static BigInteger arbitrary_ex1_b() {
		return CR.valueOf("0.1",10).approximate(-50);
	}

	public static BigInteger arbitrary_ex2_b() {
		return CR.valueOf(1000000000).multiply(CR.valueOf(1).divide(pow(CR.valueOf(2), 50))).approximate(-50);
	}

    public static CR polyforb(CR x) {
		return ((CR.valueOf(8).multiply(pow(x, 10))).add(CR.valueOf(-6).multiply(pow(x,3)))).add(CR.valueOf(-4).multiply(pow(x,2)));
	}

    public static BigInteger arbitrary_ex3_b() {
		return polyforb(CR.valueOf(-10)).approximate(-50);
	}

	public static BigInteger arbitrary_ex4_b() {
		return polyforb(CR.valueOf(0)).approximate(-50);
		
	}

	public static BigInteger arbitrary_ex5_b() {
		return polyforb(CR.valueOf("99.5",10)).approximate(-50);
		
	}

}
