import java.util.ArrayList;
import java.util.Scanner;

/**
 * Runs the Chris chatbot and manages todos, deadlines, and events.
 */
public class Chris {
    private static final String SEPARATOR = "____________________________________________________________";

    /**
     * Starts the chatbot, processes commands, and exits on {@code bye}.
     *
     * @param args command-line arguments; not used by this application
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Task> tasks = new ArrayList<>();

        showGreeting();
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            try {
                CommandType commandType = CommandType.from(input);
                if (commandType == CommandType.BYE) {
                    showFarewell();
                    break;
                } else if (commandType == CommandType.LIST) {
                    showTasks(tasks);
                } else if (commandType == CommandType.MARK) {
                    int taskIndex = getTaskIndex(input, "mark", tasks.size());
                    tasks.get(taskIndex).markAsDone();
                    showTaskMarked(tasks.get(taskIndex));
                } else if (commandType == CommandType.UNMARK) {
                    int taskIndex = getTaskIndex(input, "unmark", tasks.size());
                    tasks.get(taskIndex).markAsNotDone();
                    showTaskUnmarked(tasks.get(taskIndex));
                } else if (commandType == CommandType.TODO) {
                    String description = input.substring(4).trim();
                    if (description.isEmpty()) {
                        throw new ChrisException("A todo needs a description, e.g., todo read book.");
                    }
                    Task todo = new Todo(description);
                    tasks.add(todo);
                    showTaskAdded(todo, tasks.size());
                } else if (commandType == CommandType.DEADLINE) {
                    int byIndex = input.indexOf(" /by ");
                    if (byIndex < 0) {
                        throw new ChrisException("A deadline needs '/by', e.g., deadline return book /by Sunday.");
                    }
                    String description = input.substring(8, byIndex).trim();
                    String by = input.substring(byIndex + 5).trim();
                    if (description.isEmpty() || by.isEmpty()) {
                        throw new ChrisException("A deadline needs both a description and a time after '/by'.");
                    }
                    Task deadline = new Deadline(description, by);
                    tasks.add(deadline);
                    showTaskAdded(deadline, tasks.size());
                } else if (commandType == CommandType.EVENT) {
                    int fromIndex = input.indexOf(" /from ");
                    int toIndex = input.indexOf(" /to ");
                    if (fromIndex < 0 || toIndex < 0 || toIndex < fromIndex) {
                        throw new ChrisException("An event needs '/from' and '/to' times.");
                    }
                    String description = input.substring(5, fromIndex).trim();
                    String from = input.substring(fromIndex + 7, toIndex).trim();
                    String to = input.substring(toIndex + 5).trim();
                    if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
                        throw new ChrisException("An event needs a description, start time, and end time.");
                    }
                    Task event = new Event(description, from, to);
                    tasks.add(event);
                    showTaskAdded(event, tasks.size());
                } else if (commandType == CommandType.DELETE) {
                    int taskIndex = getTaskIndex(input, "delete", tasks.size());
                    Task deletedTask = tasks.remove(taskIndex);
                    showTaskDeleted(deletedTask, tasks.size());
                } else {
                    throw new ChrisException("I don't recognise that command. Try todo, deadline, event, list, mark, unmark, delete, or bye.");
                }
            } catch (ChrisException exception) {
                showError(exception.getMessage());
            }
        }
    }

    /** Converts a user-facing task number into a valid array index. */
    private static int getTaskIndex(String input, String command, int taskCount) throws ChrisException {
        String numberText = input.substring(command.length()).trim();
        if (numberText.isEmpty()) {
            throw new ChrisException("Please provide a task number after '" + command + "'.");
        }
        try {
            int taskNumber = Integer.parseInt(numberText);
            if (taskNumber < 1 || taskNumber > taskCount) {
                throw new ChrisException("Task " + taskNumber + " is not in the list.");
            }
            return taskNumber - 1;
        } catch (NumberFormatException exception) {
            throw new ChrisException("The task number must be a whole number.");
        }
    }

    /** Displays a user-friendly input error. */
    private static void showError(String message) {
        System.out.println(SEPARATOR);
        System.out.println(" OOPS!!! " + message);
        System.out.println(SEPARATOR);
    }

    /** Displays the chatbot's welcome message. */
    private static void showGreeting() {
        System.out.println(SEPARATOR);
        System.out.println(" Hello! I'm Chris");
        System.out.println(" What can I do for you?");
        System.out.println(SEPARATOR);
    }

    /** Displays the chatbot's goodbye message. */
    private static void showFarewell() {
        System.out.println(SEPARATOR);
        System.out.println(" Bye. Hope to see you again soon!");
        System.out.println(SEPARATOR);
    }

    /** Confirms that a task was added. */
    private static void showTaskAdded(Task task, int taskCount) {
        String taskWord = taskCount == 1 ? "task" : "tasks";
        System.out.println(SEPARATOR);
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + taskCount + " " + taskWord + " in the list.");
        System.out.println(SEPARATOR);
    }

    /** Displays all stored tasks in the order they were added. */
    private static void showTasks(ArrayList<Task> tasks) {
        System.out.println(SEPARATOR);
        System.out.println(" Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(" " + (i + 1) + "." + tasks.get(i));
        }
        System.out.println(SEPARATOR);
    }

    /** Confirms that a task was marked as completed. */
    private static void showTaskMarked(Task task) {
        System.out.println(SEPARATOR);
        System.out.println(" Nice! I've marked this task as done:");
        System.out.println("   " + task);
        System.out.println(SEPARATOR);
    }

    /** Confirms that a task was marked as incomplete. */
    private static void showTaskUnmarked(Task task) {
        System.out.println(SEPARATOR);
        System.out.println(" OK, I've marked this task as not done yet:");
        System.out.println("   " + task);
        System.out.println(SEPARATOR);
    }

    /** Confirms that a task was deleted and reports the new list size. */
    private static void showTaskDeleted(Task task, int taskCount) {
        String taskWord = taskCount == 1 ? "task" : "tasks";
        System.out.println(SEPARATOR);
        System.out.println(" Noted. I've removed this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + taskCount + " " + taskWord + " in the list.");
        System.out.println(SEPARATOR);
    }
}
