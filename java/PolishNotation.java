
/* This file implements Polish Notation
 */

import java.util.Stack;
import PLDI.TernaryBoehmReal;
import PLDI.DyadicCode;
import PLDI.Examples;

public class PolishNotation {
 
 public int evaluate(String[] tokens) {
  int result = 0;
  String operators = "+-*/";
  Stack<String> stack = new Stack<String>();
  
  for(String t : tokens){
   if(!operators.contains(t)){
    stack.push(t);
   }else{
    int a = Integer.valueOf(stack.pop());
    int b = Integer.valueOf(stack.pop());
    int index = operators.indexOf(t);
    switch(index){
     case 0:
      stack.push(String.valueOf(a+b));
      break;
     case 1:
      stack.push(String.valueOf(b-a));
      break;
     case 2:
      stack.push(String.valueOf(a*b));
      break;
     case 3:
      stack.push(String.valueOf(b/a));
      break;
    }
   }
  }
  
  result = Integer.valueOf(stack.pop());
  
  return result;
 }

 public TernaryBoehmReal evaluateReal(String[] tokens) {
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

 public static void main(String[] args) {
  PolishNotation pn = new PolishNotation();
  String[] tokens = new String[]{"10" , "10" , "*" , "5" , "-" , "537" , "*" , "33212" , "+" , "2" , "/"};
  System.out.println(pn.evaluateReal(tokens).toString());

 }

}
