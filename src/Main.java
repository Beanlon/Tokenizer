import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Command: ");
        String input = sc.nextLine().trim();

        String[] tokens = CommandParser.tokenizeInput(input);
        String[] outVars = {"", "", "", "", "", ""};

        if (CommandParser.parseCommand(tokens, outVars)) {
            CommandParser.printClassification(outVars);
            CommandParser.printDerivation(outVars);
        }
    }
}
