package PLDI;
/* This file implements Polish Notation
 */

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

public class PolishNotation {
 
  /*
   * This method evaluates a Polish Notation expression and returns a 
   * TernaryBoehmReal
   */
 public static TernaryBoehmReal evaluateReal(String[] tokens) {
  TernaryBoehmReal result = null;
  String operators = "+-*/";
  Stack<TernaryBoehmReal> stack = new Stack<TernaryBoehmReal>();
  
  for(String t : tokens){
   if(!operators.contains(t)){
    stack.push(new TernaryBoehmReal(Integer.valueOf(t)));
   } else{
    TernaryBoehmReal a = stack.pop();
    TernaryBoehmReal b = stack.pop();
    int index = operators.indexOf(t);
    switch(index){
     case 0:
      stack.push(a.add(b));
      break;
     case 1:
      stack.push(b.subtract(a));
      break;
     case 2:
      stack.push(a.multiply(b));
      break;
     case 3:
      stack.push(b.divide(a));
      break;
    }
   }
  }
  
  result = stack.pop();
  return result;
 }

  /*
   * Given a function code and a ternary boehm real, this functions evaluates the
   * function code at the given ternary boehm real.
   */
  public TernaryBoehmReal evaluate(FunctionCode f, TernaryBoehmReal x) {
    return f.F_star(Arrays.asList(x));
  }

 /*
  * This function takes a polynomial with a single variable in polish notation, 
  * and a ternary boehm real and evaluates the polynomial at the given ternary 
  * boehm real.
  */
  public TernaryBoehmReal evaluatePolynomial(String[] tokens, TernaryBoehmReal x) {
    TernaryBoehmReal result = null;
    String operators = "+-*/";
    Stack<TernaryBoehmReal> stack = new Stack<TernaryBoehmReal>();
    
    for(String t : tokens){
     if(!operators.contains(t)){
      if (t.equals("x")) {
        stack.push(x);
      } else {
        stack.push(new TernaryBoehmReal(Integer.valueOf(t)));
      }
     } else{
      TernaryBoehmReal a = stack.pop();
      TernaryBoehmReal b = stack.pop();
      int index = operators.indexOf(t);
      switch(index){
       case 0:
        stack.push(a.add(b));
        break;
       case 1:
        stack.push(b.subtract(a));
        break;
       case 2:
        stack.push(a.multiply(b));
        break;
       case 3:
        stack.push(a.divide(b));
        break;
      }
     }
    }
    
    result = stack.pop();
    return result;
  }

  /*
   * This function uncurries the above function, taking a list of tokens and
   * returning a function which takes a ternary boehm real and evaluates the
   * polynomial at the given ternary boehm real.
   */
  public Function<TernaryBoehmReal,TernaryBoehmReal> constructPolynomial(String[] tokens) {
    return x -> evaluatePolynomial(tokens, x);  }

  /*
   * This function reimplements the evaluatePolynomial function above, but
   * returns a FunctionCode instead of a TernaryBoehmReal.
   */
  public static FunctionCode constructPolynomialFunctionCode(String[] tokens) {
    FunctionCode result = null;
    String operators = "+-*/";
    Stack<FunctionCode> stack = new Stack<FunctionCode>();

    for(String t : tokens){
     if(!operators.contains(t)){
      if (t.equals("x")) {
        stack.push(FunctionCode.proj(1,0));
      } else {
        stack.push(FunctionCode.constant(1 , new TernaryBoehmReal(Integer.valueOf(t))));
      }
     } else {
      FunctionCode a = stack.pop();
      FunctionCode b = stack.pop();
      int index = operators.indexOf(t);
      switch(index){
       case 0:
        List<FunctionCode> b2 = Arrays.asList(a,b);
        stack.push(FunctionCode.sum(0 , b2));
        break;
       case 1:
        List<FunctionCode> b3 = Arrays.asList(a,b);
        stack.push(FunctionCode.multiply(0 , b3));
        break;
       case 2:
        
        break;
       case 3:
        
        break;
      }
     }
    }
    result = stack.pop();
    return result;
  }
}