package chris;

import java.util.List;
import java.util.Scanner;

/** Handles all interaction with the user. */
public class Ui {
    private static final String SEPARATOR = "____________________________________________________________";
    private final Scanner scanner = new Scanner(System.in);

    /** Returns whether another command is available. */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /** Reads the next command. */
    public String readCommand() {
        return scanner.nextLine();
    }

    /** Displays a message between separator lines. */
    public void showMessage(String... lines) {
        System.out.println(SEPARATOR);
        for (String line : lines) {
            System.out.println(line);
        }
        System.out.println(SEPARATOR);
    }

    /** Displays the numbered task list. */
    public void showTasks(List<Task> tasks) {
        System.out.println(SEPARATOR);
        System.out.println(" Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(" " + (i + 1) + "." + tasks.get(i));
        }
        System.out.println(SEPARATOR);
    }
}
