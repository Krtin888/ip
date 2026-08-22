import java.util.Scanner;

/**
 * Runs the Chris chatbot.
 */
public class Chris {
    private static final String SEPARATOR = "____________________________________________________________";
    private static final int MAX_TASKS = 100;

    /**
     * Greets the user and exits when the user enters {@code bye}.
     *
     * @param args command-line arguments; not used by this application
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

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
            } else if (input.equals("list")) {
                System.out.println(SEPARATOR);
                System.out.println(" Here are the tasks in your list:");
                for (int i = 0; i < taskCount; i++) {
                    System.out.println(" " + (i + 1) + "." + tasks[i]);
                }
                System.out.println(SEPARATOR);
            } else if (input.startsWith("mark ")) {
                int taskIndex = Integer.parseInt(input.substring(5)) - 1;
                tasks[taskIndex].markAsDone();
                System.out.println(SEPARATOR);
                System.out.println(" Nice! I've marked this task as done:");
                System.out.println("   " + tasks[taskIndex]);
                System.out.println(SEPARATOR);
            } else if (input.startsWith("unmark ")) {
                int taskIndex = Integer.parseInt(input.substring(7)) - 1;
                tasks[taskIndex].markAsNotDone();
                System.out.println(SEPARATOR);
                System.out.println(" OK, I've marked this task as not done yet:");
                System.out.println("   " + tasks[taskIndex]);
                System.out.println(SEPARATOR);
            } else {
                tasks[taskCount] = new Task(input);
                taskCount++;
                System.out.println(SEPARATOR);
                System.out.println(" added: " + input);
                System.out.println(SEPARATOR);
            }
        }
    }
}
