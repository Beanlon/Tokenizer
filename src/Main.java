import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String[] outVars = new String[6]; // [action, topic, level, constraintKey, constraintVal, resource]
        boolean success = false;

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