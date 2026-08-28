import java.util.ArrayList;
import java.time.format.DateTimeParseException;

/**
 * Runs the Chris chatbot and manages todos, deadlines, and events.
 */
public class Chris {
    private static final String DATA_FILE_PATH = "data/chris.txt";
    private final Parser parser = new Parser();
    private final Storage storage = new Storage(DATA_FILE_PATH);
    private final Ui ui = new Ui();
    private TaskList tasks;

    /**
     * Starts the chatbot, processes commands, and exits on {@code bye}.
     *
     * @param args command-line arguments; not used by this application
     */
    public static void main(String[] args) {
        new Chris().run();
    }

    /** Loads saved tasks and processes commands until the user exits. */
    public void run() {
        try {
            tasks = new TaskList(storage.load());
        } catch (ChrisException exception) {
            ui.showMessage(" OOPS!!! " + exception.getMessage());
            tasks = new TaskList(new ArrayList<>());
        }
        ui.showMessage(" Hello! I'm Chris", " What can I do for you?");
        while (ui.hasNextCommand()) {
            String input = ui.readCommand();
            try {
                CommandType commandType = parser.parseCommandType(input);
                if (commandType == CommandType.BYE) {
                    ui.showMessage(" Bye. Hope to see you again soon!");
                    break;
                } else if (commandType == CommandType.LIST) {
                    ui.showTasks(tasks.asList());
                } else if (commandType == CommandType.MARK) {
                    int taskIndex = parser.parseTaskIndex(input, "mark", tasks.size());
                    tasks.get(taskIndex).markAsDone();
                    saveTasks();
                    ui.showMessage(" Nice! I've marked this task as done:", "   " + tasks.get(taskIndex));
                } else if (commandType == CommandType.UNMARK) {
                    int taskIndex = parser.parseTaskIndex(input, "unmark", tasks.size());
                    tasks.get(taskIndex).markAsNotDone();
                    saveTasks();
                    ui.showMessage(" OK, I've marked this task as not done yet:", "   " + tasks.get(taskIndex));
                } else if (commandType == CommandType.TODO) {
                    String description = input.substring(4).trim();
                    if (description.isEmpty()) {
                        throw new ChrisException("A todo needs a description, e.g., todo read book.");
                    }
                    Task todo = new Todo(description);
                    tasks.add(todo);
                    saveTasks();
                    showTaskAdded(todo);
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
                    saveTasks();
                    showTaskAdded(deadline);
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
                    saveTasks();
                    showTaskAdded(event);
                } else if (commandType == CommandType.DELETE) {
                    int taskIndex = parser.parseTaskIndex(input, "delete", tasks.size());
                    Task deletedTask = tasks.delete(taskIndex);
                    saveTasks();
                    showTaskDeleted(deletedTask);
                } else {
                    throw new ChrisException("I don't recognise that command. Try todo, deadline, event, list, mark, unmark, delete, or bye.");
                }
            } catch (DateTimeParseException exception) {
                ui.showMessage(" OOPS!!! Use dates and times in yyyy-MM-dd HHmm format, e.g., 2026-08-30 1800.");
            } catch (ChrisException exception) {
                ui.showMessage(" OOPS!!! " + exception.getMessage());
            }
        }
    }

    private void saveTasks() throws ChrisException {
        storage.save(tasks.asList());
    }

    /** Confirms that a task was added. */
    private void showTaskAdded(Task task) {
        String taskWord = tasks.size() == 1 ? "task" : "tasks";
        ui.showMessage(" Got it. I've added this task:", "   " + task,
                " Now you have " + tasks.size() + " " + taskWord + " in the list.");
    }

    /** Confirms that a task was deleted and reports the new list size. */
    private void showTaskDeleted(Task task) {
        String taskWord = tasks.size() == 1 ? "task" : "tasks";
        ui.showMessage(" Noted. I've removed this task:", "   " + task,
                " Now you have " + tasks.size() + " " + taskWord + " in the list.");
    }
}
package chris;
