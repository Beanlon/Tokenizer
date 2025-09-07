import java.util.ArrayList;
import java.util.List;

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

    // New version: collect errors in a list
    public static boolean parseCommand(String[] tokens, String[] outVars, List<String> errors) {
        // outVars: [action, topic, level, constraintKey, constraintVal, resource]
        int i = 0;

        // <action>
        if (i < tokens.length && isInArray(tokens[i], CommandArrays.ACTIONS)) {
            outVars[0] = toCanonical(tokens[i], CommandArrays.ACTIONS);
        } else errors.add("Expected <action> at position " + (i+1));
        i++;

        // "the"
        if (i < tokens.length && tokens[i].equalsIgnoreCase("the")) { }
        else errors.add("Expected 'the' at position " + (i+1));
        i++;

        // <topic>
        if (i < tokens.length && (
                isInArray(tokens[i], CommandArrays.CONCEPTS)
                        || isInArray(tokens[i], CommandArrays.STRUCTURES)
                        || isInArray(tokens[i], CommandArrays.PROBLEM_TYPES)
        )) {
            if (isInArray(tokens[i], CommandArrays.CONCEPTS))
                outVars[1] = toCanonical(tokens[i], CommandArrays.CONCEPTS);
            else if (isInArray(tokens[i], CommandArrays.STRUCTURES))
                outVars[1] = toCanonical(tokens[i], CommandArrays.STRUCTURES);
            else
                outVars[1] = toCanonical(tokens[i], CommandArrays.PROBLEM_TYPES);
        } else errors.add("Expected <topic> at position " + (i+1));
        i++;

        // "in"
        if (i < tokens.length && tokens[i].equalsIgnoreCase("in")) {}
        else errors.add("Expected 'in' at position " + (i+1));
        i++;

        // <level>
        if (i < tokens.length && isInArray(tokens[i], CommandArrays.LEVELS)) {
            outVars[2] = toCanonical(tokens[i], CommandArrays.LEVELS);
        } else errors.add("Expected <level> at position " + (i+1));
        i++;

        // "mode"
        if (i < tokens.length && tokens[i].equalsIgnoreCase("mode")) { }
        else errors.add("Expected 'mode' at position " + (i+1));
        i++;

        // constraint: "using" <language> | "without" <concept>
        if (i < tokens.length && isInArray(tokens[i], CommandArrays.CONSTRAINT_KEYWORDS)) {
            outVars[3] = toCanonical(tokens[i], CommandArrays.CONSTRAINT_KEYWORDS); // using/without
            i++;
            if (outVars[3].equals("using")) {
                if (i < tokens.length && isInArray(tokens[i], CommandArrays.LANGUAGES)) {
                    outVars[4] = toCanonical(tokens[i], CommandArrays.LANGUAGES);
                } else errors.add("Expected <language> after 'using' at position " + (i+1));
            } else if (outVars[3].equals("without")) {
                if (i < tokens.length && isInArray(tokens[i], CommandArrays.CONCEPTS)) {
                    outVars[4] = toCanonical(tokens[i], CommandArrays.CONCEPTS);
                } else errors.add("Expected <concept> after 'without' at position " + (i+1));
            }
        } else errors.add("Expected constraint ('using' or 'without') at position " + (i+1));
        i++;

        // comma
        if (i < tokens.length && tokens[i].equals(",")) {}
        else errors.add("Expected ',' at position " + (i+1));
        i++;

        // <resource> as phrase: "with example", "with hints", "with diagram"
        if (i+1 < tokens.length && tokens[i].equalsIgnoreCase("with")) {
            String candidate = "with " + tokens[i+1];
            if (isInArray(candidate, CommandArrays.RESOURCES)) {
                outVars[5] = candidate;
                i += 2;
            } else errors.add("Expected <resource> phrase at position " + (i+1));
        } else errors.add("Expected 'with' at position " + (i+1));
        i++;

        // No extra tokens
        if (i < tokens.length) {
            for (int j = i; j < tokens.length; j++) {
                errors.add("Unexpected extra token at position " + (j+1) + ": " + tokens[j]);
            }
        }

        return errors.isEmpty();
    }

    // Retain original error() for legacy
    public static void error(String msg) {
        System.err.println("Error: " + msg);
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
        System.out.println(". → (period)");
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
        System.out.println("⇒ " + action + " the " + topic + " in " + level + " \"mode\" <constraints> \",\" <resource> \".\"");

        System.out.print("⇒ " + action + " the " + topic + " in " + level + " mode ");

        // Logic for constraintKey
        if ("using".equals(constraintKey)) {
            System.out.print("<constraint_keywords> <language>");
        } else if ("without".equals(constraintKey)) {
            System.out.print("<constraint_keywords> <concepts>");
        }

        System.out.println(" \",\" <resource> \".\"");
        System.out.println("⇒ " + action + " the " + topic + " in " + level + " mode " +
                constraintFull + "\",\" <resource> \".\"");
        System.out.println("⇒ " + action + " the " + topic + " in " + level + " mode " +
                constraintFull + ", <resource> \".\"");
        System.out.println("⇒ " + action + " the " + topic + " in " + level + " mode " +
                constraintFull + ", " + resource + " \".\"");
        System.out.println("⇒ " + commandFinal);
    }
}