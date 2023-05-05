package PLDI;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Searchers {
	
	public static PredicateCode searchPFinCompact(PredicateCode P, FunctionCode F, SpecificIntervalCode ki) {
		return new PredicateCode(x -> P.getPredicate().apply(F.F_star(Arrays.asList(x))),
								 F.getUniformContinuityOracle(ki).apply(P.delta).get(0));
	}
	
	public static PredicateCode optimiseFunCompactNaively(FunctionCode F, SpecificIntervalCode compact, int epsilon, boolean minimise) {
		List<SpecificIntervalCode> argList = intervalFunList(F, compact, epsilon);
		PredicateCode P;
		if (minimise) {
			SpecificIntervalCode min = argList.subList(1, argList.size()).stream().reduce(argList.get(0), 
											(x,y) -> new SpecificIntervalCode(x.getLeftEndpoint().min(y.getLeftEndpoint())));
			P = PredicateCode.leq(new TernaryBoehmReal(min.getLeftEndpoint()), epsilon);
		} else {
			SpecificIntervalCode max = argList.subList(1, argList.size()).stream().reduce(argList.get(0), 
					(x,y) -> new SpecificIntervalCode(x.getLeftEndpoint().max(y.getLeftEndpoint())));
			P = PredicateCode.leq(new TernaryBoehmReal(max.getLeftEndpoint()), epsilon);		}
		return searchPFinCompact(P, F, compact);
	}
	
	private static List<SpecificIntervalCode> intervalList(SpecificIntervalCode compact, int prec) {
		List<SpecificIntervalCode> list = new ArrayList<>();
		BigInteger current = compact.downLeft(prec - compact.getPrec()).getCode();
		BigInteger end = compact.downRight(prec - compact.getPrec()).getCode();
		while (current.compareTo(end) < 1) {
			list.add(new SpecificIntervalCode(new DyadicCode(current,prec)));
			current = current.add(BigInteger.TWO);
		}
		return list;
	}
	
	private static List<SpecificIntervalCode> intervalFunList(FunctionCode F, SpecificIntervalCode compact, int epsilon) {
		int delta = F.getUniformContinuityOracle(compact).apply(epsilon).get(0);
		List<SpecificIntervalCode> inputList = intervalList(compact, delta);
		Function<SpecificIntervalCode,SpecificIntervalCode> F_specific = 
				s -> new SpecificIntervalCode(F.F_star(Arrays.asList(new TernaryBoehmReal(s.getLeftEndpoint()))),epsilon);
		return inputList.stream().map(F_specific).toList();
	}
	
	public static TernaryBoehmReal exhaustive_search_naive(PredicateCode P, SpecificIntervalCode compact) {
		List<SpecificIntervalCode> searchList = intervalList(compact, P.delta);
		System.out.println(searchList);
		for (SpecificIntervalCode current : searchList) {
			TernaryBoehmReal x = new TernaryBoehmReal(current.getLeftEndpoint());
			if (P.getPredicate().apply(x)) {
				return x;
			}
		}
		return new TernaryBoehmReal(666);
	}
	
	public static TernaryBoehmReal exhaustive_search_semidecidable(PredicateCode P, SpecificIntervalCode compact, 
														  BiFunction<Integer,TernaryBoehmReal,Boolean> semipreds) {
		ArrayList<SpecificIntervalCode> frontier = new ArrayList<>(Arrays.asList(compact));
		while (!frontier.isEmpty()) {
			SpecificIntervalCode currentS = frontier.get(0);
			int currentDelta = currentS.getPrec();
			TernaryBoehmReal currentR = new TernaryBoehmReal(currentS);
			frontier.remove(0);
			if (semipreds.apply(currentDelta,currentR)) {
				if (currentDelta == P.delta && P.getPredicate().apply(currentR)) {
					return currentR;
				}
				frontier.add(0, currentS.downLeft());
				frontier.add(0, currentS.downMid());
				frontier.add(0, currentS.downRight());
			} else {
				frontier.add(currentS.downLeft());
				frontier.add(currentS.downMid());
				frontier.add(currentS.downRight());
			}
		}
		return new TernaryBoehmReal(666);
	}
	
	private static boolean eclipses(VariableIntervalCode fx, VariableIntervalCode fy) {
		if (fx.getPrec() > fy.getPrec()) {
			fy = fy.down(fx.getPrec() - fy.getPrec());
		} else if (fx.getPrec() < fy.getPrec()) {
			fx = fx.down(fy.getPrec() - fx.getPrec());
		}
		return (fx.getRightCode().compareTo(fy.getLeftCode()) < 0);
	}

	public static TernaryBoehmReal minimise(FunctionCode F, SpecificIntervalCode compact, int epsilon) {
		int delta = F.getUniformContinuityOracle(compact).apply(epsilon).get(0);
		System.out.println(delta);
		VariableIntervalCode fcompact = F.apply(Arrays.asList(compact.getVariableIntervalCode()));
		if (compact.getPrec() >= delta || fcompact.join_prime().getPrec() >= epsilon) {
			return new TernaryBoehmReal(compact);
		}
		ArrayList<Pair<SpecificIntervalCode,VariableIntervalCode>> frontier = new ArrayList<>();
		ArrayList<Pair<SpecificIntervalCode,VariableIntervalCode>> answers = new ArrayList<>();
		ArrayList<SpecificIntervalCode> history = new ArrayList<>();
		frontier.add(new Pair<>(compact,fcompact));
		history.add(compact);
		int checks = 0;
		while (!frontier.isEmpty()) {
			checks++;
			int index = (int) (Math.random() * frontier.size());
			SpecificIntervalCode current = frontier.get(index).getFst();
			frontier.remove(index);
			SpecificIntervalCode left = current.downLeft();
			VariableIntervalCode fleft = F.apply(Arrays.asList(left.getVariableIntervalCode()));
			SpecificIntervalCode right = current.downRight();
			VariableIntervalCode fright = F.apply(Arrays.asList(right.getVariableIntervalCode()));
			Pair<SpecificIntervalCode,VariableIntervalCode> leftfleft = new Pair<>(left,fleft);
			Pair<SpecificIntervalCode,VariableIntervalCode> rightfright = new Pair<>(right,fright);
			frontier.removeIf(y -> eclipses(fleft,y.getSnd()) || eclipses(fright,y.getSnd()));
			answers.removeIf(y -> eclipses(fleft,y.getSnd()) || eclipses(fright,y.getSnd()));
			boolean addLeft = true;
			boolean addRight = true;
			if (eclipses(fleft, fright) || history.contains(right)) {
				addRight = false;
			} 
			if ((addRight && eclipses(fright, fleft)) || history.contains(left)) {
				addLeft = false;
			}
			history.add(left);
			history.add(right);
			if (addLeft && (frontier.stream().anyMatch(y -> eclipses(y.getSnd(),fleft)) || answers.stream().anyMatch(y -> eclipses(y.getSnd(),fleft)))) {
				addLeft = false;
			}
			if (addRight && (frontier.stream().anyMatch(y -> eclipses(y.getSnd(),fright)) || answers.stream().anyMatch(y -> eclipses(y.getSnd(),fright)))) {
				addRight = false;
			}
			if (addLeft) {
				if (left.getPrec() >= delta || fleft.join_prime().getPrec() >= epsilon) {
					answers.add(leftfleft);
				} else {
					frontier.add(leftfleft);
				}
			}
			if (addRight) {
				if (right.getPrec() >= delta || fright.join_prime().getPrec() >= epsilon) {
					answers.add(rightfright);
				} else {
					frontier.add(rightfright);
				}
			}
		}
		if (answers.size() > 0) {
			System.out.println(checks);
			return new TernaryBoehmReal(answers.get(0).getFst());
		}
		return new TernaryBoehmReal(666);
	}
	
}
