import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);


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
        System.out.println("- You must include **all** keywords (the, in, mode, using/without, comma, period) in the correct order.");
        System.out.println("- The command must end with a period.");
        System.out.println();


        // Array to store the key pieces parsed from the user's input
        String[] outVars = new String[6];
        //Will track if the input was successfully parsed and classified.
        boolean success = false;


        //The loop keeps running until success is true (i.e., the user input is valid or the user quits).
        while (!success) {
            System.out.print("Enter command: ");
            String input = scanner.nextLine();

            //Prepares a list to collect errors
            List<String> errors = new ArrayList<>();

            //Splits the input string into tokens (words/phrases) based on your grammar
            //Adds any tokenization errors to the errors list
            String[] tokens = CommandParser.tokenizeInput(input, errors);

            //Resets outVars to blanks so leftover data from previous attempts doesn't linger. - So you can have new placeholders for each attempt
            for (int i = 0; i < outVars.length; i++){
                outVars[i] = "";
            }

            // Collect errors in a list
            success = CommandParser.parseCommand(tokens, outVars, errors);

            if (!success) {
                // Show errors first
                System.out.println("\nErrors detected:");
                for (String err : errors) {
                    System.out.println(" - " + err);
                }
                // Then prompt
                System.out.print("\nDo you want to try again? (yes/no): ");
                String answer = scanner.nextLine().trim().toLowerCase();
                if (!answer.equals("yes")) {
                    System.out.println("Exiting program.");
                    break;
                }
            } else {
                CommandParser.printClassification(outVars);
                CommandParser.printDerivation(outVars);
            }
        }

        scanner.close();
    }
}