import java.util.Scanner;

/**
 * Runs the Chris chatbot.
 */
public class Chris {
    private static final String SEPARATOR = "____________________________________________________________";

    /**
     * Greets the user and exits when the user enters {@code bye}.
     *
     * @param args command-line arguments; not used by this application
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(SEPARATOR);
        System.out.println(" Hello! I'm Chris");
        System.out.println(" What can I do for you?");
        System.out.println(SEPARATOR);

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            if (input.equals("bye")) {
                System.out.println(SEPARATOR);
                System.out.println(" Bye. Hope to see you again soon!");
                System.out.println(SEPARATOR);
                break;
            }
        }
    }
}
