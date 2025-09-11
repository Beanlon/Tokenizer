import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String[] outVars = new String[6]; // [action, topic, level, constraintKey, constraintVal, resource]
        boolean success = false;

        System.out.println("\n\nWelcome! Please enter your command following this exact format:");
        System.out.println("Please enter your command in the following exact format:");
        System.out.println("<action> the <topic> in <level> mode <constraint>, <resource>.");
        System.out.println("---------------------------------------------------------------");
        System.out.println("Where:");

        //1
        System.out.println("  <action>     : " + String.join(" | ", CommandArrays.ACTIONS));
        //2
        System.out.println("  <topic>      : " + String.join(" | ", CommandArrays.CONCEPTS) + " | " +
                String.join(" | ", CommandArrays.STRUCTURES) + " | " +
                String.join(" | ", CommandArrays.PROBLEM_TYPES));
        //3
        System.out.println("  <level>      : " + String.join(" | ", CommandArrays.LEVELS));

        //4
        System.out.println("  <constraint> : using <language>   OR   without <concept>");

        //5
        System.out.println("  <language>   : " + String.join(" | ", CommandArrays.LANGUAGES));
        System.out.println("  <concept>    : " + String.join(" | ", CommandArrays.CONCEPTS));

        //6
        System.out.println("  <resource>   : " + String.join(" | ", CommandArrays.RESOURCES));

        System.out.println("---------------------------------------------------------------");
        System.out.println("Important:");
        System.out.println("- You must include *all* keywords (the, in, mode, using/without, comma, period) in the correct order.");
        System.out.println("- The command must end with a period.");
        System.out.println();

        while (!success) {
            System.out.print("Enter command: ");
            String input = scanner.nextLine();
            List<String> errors = new ArrayList<>();
            String[] tokens = CommandParser.tokenizeInput(input,errors);

            // Reset outVars before each attempt
            for (int i = 0; i < outVars.length; i++) outVars[i] = "";

            // Collect errors in a list
            success = CommandParser.parseCommand(tokens, outVars, errors);

            if (!success) {
                // Show errors first
                System.out.println("\nErrors detected:");
                for (String err : errors) {
                    System.out.println(" - " + err);
                }

                boolean isValid = false;
                // Then prompt user to try again or exit
                while(!isValid){
                    System.out.print("\nDo you want to try again? (yes/ no): ");
                    String answer = scanner.nextLine().trim().toLowerCase();
                    if (answer.equals("yes")) {
                        isValid = true; // valid input, continue
                    } else if (answer.equals("no")) {
                        System.out.println("Exiting program.");
                        System.exit(0);
                    } else {
                        System.out.println("Invalid input please type 'yes' or 'no");
                        continue;
                    }
                }
            } else {
                CommandParser.printClassification(outVars);
                CommandParser.printDerivation(outVars);
            }
        }

        scanner.close();
    }
}