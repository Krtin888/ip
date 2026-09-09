package chris;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/** Runs the Chris chatbot and manages todos, deadlines, and events. */
public class Chris {
    private static final String DATA_FILE_PATH = "data/chris.txt";
    private final Parser parser;
    private final Storage storage;
    private final Ui ui;
    private TaskList tasks;
    private boolean isExitRequested;

    /** Creates Chris using its default data file. */
    public Chris() {
        this(DATA_FILE_PATH);
    }

    /** Creates Chris using the specified data file. */
    public Chris(String dataFilePath) {
        parser = new Parser();
        storage = new Storage(dataFilePath);
        ui = new Ui();
    }

    /**
     * Starts the chatbot, processes commands, and exits on {@code bye}.
     *
     * @param args command-line arguments; not used by this application
     */
    public static void main(String[] args) {
        new Chris().run();
    }

    /** Loads saved tasks and processes console commands until the user exits. */
    public void run() {
        initializeTasks(ui);
        ui.showMessage(" Hello! I'm Chris", " What can I do for you?");
        while (ui.hasNextCommand()) {
            executeCommand(ui.readCommand(), ui);
            if (isExitRequested) {
                break;
            }
        }
    }

    /**
     * Starts a GUI session and returns the initial message.
     *
     * @return greeting and any warning generated while loading saved tasks
     */
    public String startGui() {
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        Ui responseUi = createResponseUi(outputBytes);
        initializeTasks(responseUi);
        responseUi.showMessage("Hello! I'm Chris", "What can I do for you?");
        return outputBytes.toString(StandardCharsets.UTF_8).stripTrailing();
    }

    /**
     * Executes one GUI command and returns Chris's response.
     *
     * @param input command entered by the user
     * @return response to the command
     */
    public String getResponse(String input) {
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        Ui responseUi = createResponseUi(outputBytes);
        initializeTasks(responseUi);
        executeCommand(input, responseUi);
        return outputBytes.toString(StandardCharsets.UTF_8).stripTrailing();
    }

    /** Returns whether the most recent command requested an exit. */
    public boolean isExitRequested() {
        return isExitRequested;
    }

    private void initializeTasks(Ui outputUi) {
        if (tasks != null) {
            return;
        }
        try {
            tasks = new TaskList(storage.load());
        } catch (ChrisException exception) {
            outputUi.showMessage(" OOPS!!! " + exception.getMessage());
            tasks = new TaskList(new ArrayList<>());
        }
    }

    private void executeCommand(String input, Ui outputUi) {
        isExitRequested = false;
        try {
            CommandType commandType = parser.parseCommandType(input);
            if (commandType == CommandType.BYE) {
                outputUi.showMessage(" Bye. Hope to see you again soon!");
                isExitRequested = true;
            } else if (commandType == CommandType.LIST) {
                outputUi.showTasks(tasks.asList());
            } else if (commandType == CommandType.MARK) {
                int taskIndex = parser.parseTaskIndex(input, "mark", tasks.size());
                tasks.get(taskIndex).markAsDone();
                saveTasks();
                outputUi.showMessage(" Nice! I've marked this task as done:", "   " + tasks.get(taskIndex));
            } else if (commandType == CommandType.UNMARK) {
                int taskIndex = parser.parseTaskIndex(input, "unmark", tasks.size());
                tasks.get(taskIndex).markAsNotDone();
                saveTasks();
                outputUi.showMessage(" OK, I've marked this task as not done yet:", "   " + tasks.get(taskIndex));
            } else if (commandType == CommandType.TODO) {
                String description = input.substring(4).trim();
                if (description.isEmpty()) {
                    throw new ChrisException("A todo needs a description, e.g., todo read book.");
                }
                Task todo = new Todo(description);
                tasks.add(todo);
                saveTasks();
                showTaskAdded(todo, outputUi);
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
                showTaskAdded(deadline, outputUi);
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
                showTaskAdded(event, outputUi);
            } else if (commandType == CommandType.DELETE) {
                int taskIndex = parser.parseTaskIndex(input, "delete", tasks.size());
                Task deletedTask = tasks.delete(taskIndex);
                saveTasks();
                showTaskDeleted(deletedTask, outputUi);
            } else if (commandType == CommandType.FIND) {
                String keyword = input.substring(4).trim();
                if (keyword.isEmpty()) {
                    throw new ChrisException("Please provide a keyword after 'find'.");
                }
                outputUi.showTasks(tasks.find(keyword));
            } else if (commandType == CommandType.HELP) {
                outputUi.showMessage(" Commands:",
                        " todo DESCRIPTION",
                        " deadline DESCRIPTION /by yyyy-MM-dd HHmm",
                        " event DESCRIPTION /from yyyy-MM-dd HHmm /to yyyy-MM-dd HHmm",
                        " list | find KEYWORD | mark NUMBER | unmark NUMBER | delete NUMBER | bye");
            } else {
                throw new ChrisException("I don't recognise that command. Try todo, deadline, event, "
                        + "list, find, mark, unmark, delete, help, or bye.");
            }
        } catch (DateTimeParseException exception) {
            outputUi.showMessage(" OOPS!!! Use dates and times in yyyy-MM-dd HHmm format, "
                    + "e.g., 2026-08-30 1800.");
        } catch (ChrisException exception) {
            outputUi.showMessage(" OOPS!!! " + exception.getMessage());
        }
    }

    private void saveTasks() throws ChrisException {
        storage.save(tasks.asList());
    }

    private void showTaskAdded(Task task, Ui outputUi) {
        String taskWord = tasks.size() == 1 ? "task" : "tasks";
        outputUi.showMessage(" Got it. I've added this task:", "   " + task,
                " Now you have " + tasks.size() + " " + taskWord + " in the list.");
    }

    private void showTaskDeleted(Task task, Ui outputUi) {
        String taskWord = tasks.size() == 1 ? "task" : "tasks";
        outputUi.showMessage(" Noted. I've removed this task:", "   " + task,
                " Now you have " + tasks.size() + " " + taskWord + " in the list.");
    }

    private Ui createResponseUi(ByteArrayOutputStream outputBytes) {
        PrintStream output = new PrintStream(outputBytes, true, StandardCharsets.UTF_8);
        return new Ui(InputStream.nullInputStream(), output, false);
    }
}
