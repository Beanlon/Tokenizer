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
        // outVars: [action, topic, level, constraintKey, constraintVal, resource]
        int i = 0;
        // <action>
        if (i < tokens.length && isInArray(tokens[i], CommandArrays.ACTIONS)) {
            outVars[0] = toCanonical(tokens[i], CommandArrays.ACTIONS); i++;
        } else return error("Expected <action> at position " + (i+1));

        // "the"
        if (i < tokens.length && tokens[i].equalsIgnoreCase("the")) { i++; }
        else return error("Expected 'the' at position " + (i+1));

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
            i++;
        } else return error("Expected <topic> at position " + (i+1));

        // "in"
        if (i < tokens.length && tokens[i].equalsIgnoreCase("in")) { i++; }
        else return error("Expected 'in' at position " + (i+1));

        // <level>
        if (i < tokens.length && isInArray(tokens[i], CommandArrays.LEVELS)) {
            outVars[2] = toCanonical(tokens[i], CommandArrays.LEVELS); i++;
        } else return error("Expected <level> at position " + (i+1));

        // "mode"
        if (i < tokens.length && tokens[i].equalsIgnoreCase("mode")) { i++; }
        else return error("Expected 'mode' at position " + (i+1));

        // constraint: "using" <language> | "without" <concept>
        if (i < tokens.length && isInArray(tokens[i], CommandArrays.CONSTRAINT_KEYWORDS)) {
            outVars[3] = toCanonical(tokens[i], CommandArrays.CONSTRAINT_KEYWORDS); // using/without
            i++;
            if (outVars[3].equals("using")) {
                if (i < tokens.length && isInArray(tokens[i], CommandArrays.LANGUAGES)) {
                    outVars[4] = toCanonical(tokens[i], CommandArrays.LANGUAGES);
                    i++;
                } else return error("Expected <language> after 'using' at position " + (i+1));
            } else if (outVars[3].equals("without")) {
                if (i < tokens.length && isInArray(tokens[i], CommandArrays.CONCEPTS)) {
                    outVars[4] = toCanonical(tokens[i], CommandArrays.CONCEPTS);
                    i++;
                } else return error("Expected <concept> after 'without' at position " + (i+1));
            }
        } else return error("Expected constraint ('using' or 'without') at position " + (i+1));

        // comma
        if (i < tokens.length && tokens[i].equals(",")) { i++; }
        else return error("Expected ',' at position " + (i+1));

        // <resource> as phrase: "with example", "with hints", "with diagram"
        if (i+1 < tokens.length && tokens[i].equalsIgnoreCase("with")) {
            String candidate = "with " + tokens[i+1];
            if (isInArray(candidate, CommandArrays.RESOURCES)) {
                outVars[5] = candidate;
                i += 2;
            } else return error("Expected <resource> phrase at position " + (i+1));
        } else return error("Expected 'with' at position " + (i+1));

        // No extra tokens
        if (i < tokens.length) {
            return error("Unexpected extra token(s) at position " + (i+1) + ": " + tokens[i]);
        }
        return true;
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

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Command: ");
        String input = sc.nextLine().trim();
        String[] tokens = tokenizeInput(input);
        String[] outVars = {"", "", "", "", "", ""};
        if (parseCommand(tokens, outVars)) {
            printClassification(outVars);
            printDerivation(outVars);
        }
    }
}