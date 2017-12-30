package com.tona0516;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.combinatoradix.Permutator;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static final int MAX_CARD_IN_EQUATION = 5;

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 1, 1, 3, 3, 4, 4, 5, 6, 6, 7, 9);
        List<String> operators = Arrays.asList("-", "/", "/");

        System.out.println("#####init#####");
        System.out.println(numbers);
        System.out.println(operators);
        System.out.println("#####search#####");
        long start = System.currentTimeMillis();
        List<String> numbersStr = numbers.stream().map(s -> String.valueOf(s)).collect(Collectors.toList());
        Set<List<String>> combinations = getCombination(numbersStr, operators);
        List<List<String>> answer = getAnswer(numbersStr, operators, combinations);
        System.out.println("#####answer#####");
        System.out.println("elapsed time: " + (System.currentTimeMillis() - start) / 1000.0);
        System.out.println(numbers);
        System.out.println(operators);
        System.out.println(answer);
    }

    public static List<List<String>> getAnswer(List<String> numbers, List<String> operators, Set<List<String>> combinations) {
        List<List<String>> _combinations = new ArrayList<>(combinations);
        for (List<List<String>> c : new Combinator<>(_combinations, 3)) {
            List<String> _numbers = new ArrayList<>(numbers);
            List<String> _operators = new ArrayList<>(operators);
            List<String> card = new ArrayList<>();
            card.addAll(_numbers);
            card.addAll(_operators);
            if (isAnswer(card, c)) {
                return c;
            }
        }
        return null;
    }

    public static boolean isAnswer(List<String> card, List<List<String>> c) {
        if (c.get(0).size() + c.get(1).size() + c.get(2).size() != card.size() + 3) {
            return false;
        }
        for (List<String> _c : c) {
            for (String input : _c) {
                if (input.equals("=")) {
                    continue;
                }
                if(card.contains(input)) {
                    card.remove(input);
                } else {
                    return false;
                }
            }
        }
        System.out.println(card);
        if (!card.isEmpty()) {
            return false;
        }
        return true;
    }

    public static Set<List<String>> getCombination(List<String> numbers, List<String> operators) {
        Set<List<String>> equations = new LinkedHashSet<>();
        List<String> searchedEquations = new ArrayList<>();

        for (int i = 0; i < MAX_CARD_IN_EQUATION + 1; i++) {
            for (int j = 0; j < operators.size() + 1; j++) {
                System.out.println("Using numbers and operators size: " + i + ", " + j);
                System.out.println("Found equations size: " + equations.size());
                System.out.println("Searched equations size: " + searchedEquations.size());
                for (List<String> numberCombs : new Combinator<>(numbers, i)) {
                    for (List<String> operatorCombs : new Combinator<>(operators, j)) {
                        if (isSearchedCombination(numberCombs, operatorCombs, searchedEquations)) {
                            List<String> equation = getEquationIfIsExist(numberCombs, operatorCombs);
                            if (equation != null) {
                                equations.add(equation);
                            }
                        }
                    }
                }
            }
        }
        return equations;
    }

    public static boolean isSearchedCombination(List<String> numbers, List<String> operators, List<String> searchedList) {
        if (searchedList.contains(numbers.toString() + operators.toString())) {
            return true;
        }
        searchedList.add(numbers.toString() + operators.toString());
        return false;
    }

    public static List<String> getEquationIfIsExist(List<String> numbers, List<String> operators) {
        List<String> perms = new ArrayList<>();
        perms.addAll(numbers);
        perms.addAll(operators);
        perms.add("=");
        for (List<String> p : new Permutator<>(perms, perms.size())) {
            if (isValidEquation(p) && isCorrect(p)) {
                return p;
            }
        }
        return null;
    }

    public static boolean isValidEquation(List<String> perms) {
        String oldInput = null;
        if (!isNumber(perms.get(0)) || !isNumber(perms.get(perms.size() - 1))) {
            return false;
        }
        for (String input : perms) {
            if (!isNumber(input) && !isNumber(oldInput)) {
                return false;
            }
            oldInput = input;
        }
        return true;
    }

    public static boolean isCorrect(List<String> perms) {
        List<String> leftEquation = new ArrayList<>();
        List<String> rightEquation = new ArrayList<>();
        boolean isReadingLeft = true;
        for (String input : perms) {
            if (input.equals("=")) {
                isReadingLeft = false;
                continue;
            }
            if (isReadingLeft) {
                leftEquation.add(input);
            } else {
                rightEquation.add(input);
            }
        }
        Integer leftAnswer = calculate(fix(leftEquation));
        Integer rightAnswer = calculate(fix(rightEquation));
        if (leftAnswer != null && rightAnswer != null && leftAnswer.intValue() == rightAnswer.intValue()) {
            return true;
        } else {
            return false;
        }
    }

    public static List<String> fix(List<String> equation) {
        List<String> fixedEquation = new ArrayList<>();
        String temp = "";
        for (String input : equation) {
            if (isNumber(input)) {
                temp += input;
            } else {
                fixedEquation.add(temp);
                fixedEquation.add(input);
                temp = "";
            }
        }
        fixedEquation.add(temp);
        return fixedEquation;
    }

    public static Integer calculate(List<String> equation) {
        Stack<String> p = new Stack<>();
        Stack<String> s = new Stack<>();
        // 逆ポーランド記法に変換
        for (String input : equation) {
            while (!s.isEmpty() && compare(s.peek(), input) < 1) {
                p.push(s.pop());
            }
            s.push(input);
        }
        // 逆ポーランド記法で計算
        while (!p.empty()) {
            s.push(p.pop());
        }
        while (!s.isEmpty()) {
            if (isNumber(s.peek())) {
                p.push(s.pop());
            } else {
                int b = Integer.parseInt(p.pop());
                int a = Integer.parseInt(p.pop());
                String operator = s.pop();
                if (operator.equals("+")) {
                    p.push(String.valueOf(a+b));
                } else if (operator.equals("-")) {
                    p.push(String.valueOf(a-b));
                } else if (operator.equals("*")) {
                    p.push(String.valueOf(a*b));
                } else if (operator.equals("/") && b != 0 && a % b == 0) {
                    p.push(String.valueOf(a/b));
                } else {
                    return null;
                }
            }
        }
        return Integer.parseInt(p.pop());
    }

    /**
     * input1の優先度が高いなら1, 同じなら0, 低いなら-1を返却
     */
    public static int compare(String input1, String input2) {
        if (isNumber(input1) && isNumber(input2)) {
            return 0;
        } else if (isNumber(input1) && !isNumber(input2)) {
            return -1;
        } else if (!isNumber(input1) && isNumber(input2)) {
            return 1;
        } else {
            if ((input1.equals("+") || input1.equals("-")) && (input2.equals("*") || input2.equals("/"))) {
                return 1;
            } else if ((input1.equals("*") || input1.equals("/")) && (input2.equals("+") || input2.equals("-"))) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public static boolean isNumber(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
