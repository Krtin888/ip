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
        assert tasks != null : "Tasks must be initialized before handling commands";
        isExitRequested = false;
        try {
            CommandType commandType = parser.parseCommandType(input);
            switch (commandType) {
            case BYE -> handleBye(outputUi);
            case LIST -> outputUi.showTasks(tasks.asList());
            case MARK -> handleMark(input, outputUi);
            case UNMARK -> handleUnmark(input, outputUi);
            case TODO -> handleTodo(input, outputUi);
            case DEADLINE -> handleDeadline(input, outputUi);
            case EVENT -> handleEvent(input, outputUi);
            case DELETE -> handleDelete(input, outputUi);
            case FIND -> handleFind(input, outputUi);
            default -> throw new ChrisException("I don't recognise that command. Try todo, deadline, event, "
                    + "list, mark, unmark, delete, or bye.");
            }
        } catch (DateTimeParseException exception) {
            outputUi.showMessage(" OOPS!!! Use dates and times in yyyy-MM-dd HHmm format, "
                    + "e.g., 2026-08-30 1800.");
        } catch (ChrisException exception) {
            outputUi.showMessage(" OOPS!!! " + exception.getMessage());
        }
    }

    private void handleBye(Ui outputUi) {
        outputUi.showMessage(" Bye. Hope to see you again soon!");
        isExitRequested = true;
    }

    private void handleMark(String input, Ui outputUi) throws ChrisException {
        int taskIndex = parser.parseTaskIndex(input, "mark", tasks.size());
        tasks.get(taskIndex).markAsDone();
        saveTasks();
        outputUi.showMessage(" Nice! I've marked this task as done:", "   " + tasks.get(taskIndex));
    }

    private void handleUnmark(String input, Ui outputUi) throws ChrisException {
        int taskIndex = parser.parseTaskIndex(input, "unmark", tasks.size());
        tasks.get(taskIndex).markAsNotDone();
        saveTasks();
        outputUi.showMessage(" OK, I've marked this task as not done yet:", "   " + tasks.get(taskIndex));
    }

    private void handleTodo(String input, Ui outputUi) throws ChrisException {
        String description = input.substring(4).trim();
        if (description.isEmpty()) {
            throw new ChrisException("A todo needs a description, e.g., todo read book.");
        }
        addTask(new Todo(description), outputUi);
    }

    private void handleDeadline(String input, Ui outputUi) throws ChrisException {
        int byIndex = input.indexOf(" /by ");
        if (byIndex < 0) {
            throw new ChrisException("A deadline needs '/by', e.g., deadline return book /by Sunday.");
        }
        String description = input.substring(8, byIndex).trim();
        String by = input.substring(byIndex + 5).trim();
        if (description.isEmpty() || by.isEmpty()) {
            throw new ChrisException("A deadline needs both a description and a time after '/by'.");
        }
        addTask(new Deadline(description, by), outputUi);
    }

    private void handleEvent(String input, Ui outputUi) throws ChrisException {
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
        addTask(new Event(description, from, to), outputUi);
    }

    private void handleDelete(String input, Ui outputUi) throws ChrisException {
        int taskIndex = parser.parseTaskIndex(input, "delete", tasks.size());
        Task deletedTask = tasks.delete(taskIndex);
        saveTasks();
        showTaskDeleted(deletedTask, outputUi);
    }

    private void handleFind(String input, Ui outputUi) throws ChrisException {
        String keyword = input.substring(4).trim();
        if (keyword.isEmpty()) {
            throw new ChrisException("Please provide a keyword after 'find'.");
        }
        outputUi.showTasks(tasks.find(keyword));
    }

    private void addTask(Task task, Ui outputUi) throws ChrisException {
        tasks.add(task);
        saveTasks();
        showTaskAdded(task, outputUi);
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
