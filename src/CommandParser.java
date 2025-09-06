import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CommandParser {
    public static boolean isInArray(String value, String[] array) {
        for (String s : array) {
            if (value.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

    public static String toCanonical(String value, String[] array) {
        for (String s : array) {
            if (value.equalsIgnoreCase(s)) return s;
        }
        return value;
    }

    public static String[] tokenizeInput(String input) {
        int inLen = input.length();
        if (inLen > 0 && input.charAt(inLen-1) == '.') input = input.substring(0, inLen-1).trim();

        // Insert space before comma
        StringBuilder fixed = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == ',') fixed.append(" ,");
            else fixed.append(c);
        }
        input = fixed.toString();

        // Manual tokenization (split on spaces)
        String[] tokens = new String[30];
        int t = 0;
        int last = 0;
        for (int i = 0; i <= input.length(); i++) {
            char c = (i == input.length()) ? ' ' : input.charAt(i);
            if (c == ' ' || i == input.length()) {
                if (last < i) {
                    tokens[t++] = input.substring(last, i);
                }
                last = i + 1;
            }
        }
        String[] realTokens = new String[t];
        for (int j = 0; j < t; j++) realTokens[j] = tokens[j];
        return realTokens;
    }

    public static boolean parseCommand(String[] tokens, String[] outVars) {
        List<String> errors = new ArrayList<>();
        String[][] expectedArrays = {CommandArrays.ACTIONS, null,
                mergeArrays(CommandArrays.CONCEPTS, CommandArrays.STRUCTURES, CommandArrays.PROBLEM_TYPES), // <topic>
                null, // "in"
                CommandArrays.LEVELS, // <level>
                null, // "mode"
                CommandArrays.CONSTRAINT_KEYWORDS // constraint-keyword
        };

        String[] expectedLiterals = {null, "the", null, "in", null, "mode", null};
        int i = 0, o = 0, step = 0;
        while (step < expectedArrays.length) {
            if (expectedArrays[step] != null) {
                if (i < tokens.length && isInArray(tokens[i], expectedArrays[step])) {
                    outVars[o++] = toCanonical(tokens[i], expectedArrays[step]);
                    i++;
                } else {
                    return errors.add("Expected <" + (o == 0 ? "action" : o == 1 ?
                            "topic" : o == 2 ? "level" : "constraint") + "> at position " + (i+1));
                }
            } else if (expectedLiterals != null) {
                if (i < tokens.length && tokens[i].equalsIgnoreCase(expectedLiterals[step])) {
                    i++;
                } else return errors.add("Expected '" + expectedLiterals[step] + "' at position " + (i+1));
            }
            step++;
        }

        // Constraint value
        if (outVars[3].equals("using")) {
            if (i < tokens.length && isInArray(tokens[i], CommandArrays.LANGUAGES)) {
                outVars[4] = toCanonical(tokens[i], CommandArrays.LANGUAGES); i++;
            } else return errors.add("Expected <language> after 'using' at position " + (i+1));
        } else if (outVars[3].equals("without")) {
            if (i < tokens.length && isInArray(tokens[i], CommandArrays.CONCEPTS)) {
                outVars[4] = toCanonical(tokens[i], CommandArrays.CONCEPTS); i++;
            } else return errors.add("Expected <concept> after 'without' at position " + (i+1));
        }
        // Comma
        if (i < tokens.length && tokens[i].equals(",")) { i++; }
        else return errors.add("Expected ',' at position " + (i+1));
        // Resource
        if (i+1 < tokens.length && tokens[i].equalsIgnoreCase("with")) {
            String candidate = "with " + tokens[i+1];
            if (isInArray(candidate, CommandArrays.RESOURCES)) {
                outVars[5] = candidate;
                i += 2;
            } else return errors.add("Expected <resource> phrase at position " + (i+1));
        } else return errors.add("Expected 'with' at position " + (i+1));
        // No extra tokens
        if (i < tokens.length) {
            return errors.add("Unexpected extra token(s) at position " + (i+1) + ": " + tokens[i]);
        }
        return true;
    }

    // Helper to merge arrays
    private static String[] mergeArrays(String[]... arrays) {
        int total = 0;
        for (String[] arr : arrays) total += arr.length;
        String[] merged = new String[total];
        int idx = 0;
        for (String[] arr : arrays) {
            for (String s : arr) merged[idx++] = s;
        }
        return merged;
    }

    public static boolean error(String msg) {
        System.err.println("Error: " + msg);
        return false;
    }

    public static void printClassification(String[] outVars) {
        String action = outVars[0], topic = outVars[1], level = outVars[2];
        String constraintKey = outVars[3], constraintVal = outVars[4], resource = outVars[5];
        System.out.println("\nPhase 1: CFG-based classification\n");
        System.out.println(action + " → <action>");
        System.out.println("the → (keyword)");
        System.out.println(topic + " → <topic>");
        System.out.println("in → (keyword)");
        System.out.println(level + " → <level>");
        System.out.println("mode → (keyword)");
        if (!constraintKey.isEmpty())
            System.out.println(constraintKey + " → (constraint-keyword)");
        if (!constraintVal.isEmpty())
            System.out.println(constraintVal +
                    (isInArray(constraintVal, CommandArrays.LANGUAGES) ? " → <language>" : " → <concept>"));
        System.out.println(", → (comma)");
        if (!resource.isEmpty())
            System.out.println(resource + " → <resource>");
    }

    public static void printDerivation(String[] outVars) {
        String action = outVars[0], topic = outVars[1], level = outVars[2];
        String constraintKey = outVars[3], constraintVal = outVars[4], resource = outVars[5];
        System.out.println("\nPhase 2: Derivation (leftmost derivation example)\n");
        String constraintFull = constraintKey + " " + constraintVal;
        String commandFinal = action + " the " + topic + " in " + level + " mode " +
                constraintFull + ", " + resource + ".";
        System.out.println("<command>");
        System.out.println("⇒ <action> \"the\" <topic> \"in\" <level> \"mode\" <constraint> \",\" <resource> \".\"");
        System.out.println("⇒ " + action + " \"the\" <topic> \"in\" <level> \"mode\" <constraint> \",\" <resource> \".\"");
        System.out.println("⇒ " + action + " the " + topic + " \"in\" <level> \"mode\" <constraint> \",\" <resource> \".\"");
        System.out.println("⇒ " + action + " the " + topic + " in " + level + " \"mode\" <constraint> \",\" <resource> \".\"");
        System.out.println("⇒ " + action + " the " + topic + " in " + level + " mode " +
                constraintFull + ", <resource> \".\"");
        System.out.println("⇒ " + action + " the " + topic + " in " + level + " mode " +
                constraintFull + ", " + resource + " \".\"");
        System.out.println("⇒ " + commandFinal);
    }


}