/**
 * Represents a task and whether it has been completed.
 */
public class Task {
    protected String description;
    protected boolean isDone;
    protected char type;
    protected String by;
    protected String from;
    protected String to;

    /**
     * Creates an incomplete task with the given description.
     *
     * @param description description of the task
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
        this.type = 'T';
    }

    /**
     * Creates an incomplete deadline task.
     *
     * @param description description of the task
     * @param by deadline stored as text
     */
    public Task(String description, String by) {
        this(description);
        this.type = 'D';
        this.by = by;
    }

    /**
     * Creates an incomplete event task.
     *
     * @param description description of the task
     * @param from start time stored as text
     * @param to end time stored as text
     */
    public Task(String description, String from, String to) {
        this(description);
        this.type = 'E';
        this.from = from;
        this.to = to;
    }

    /** Marks this task as completed. */
    public void markAsDone() {
        isDone = true;
    }

    /** Marks this task as incomplete. */
    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Returns the symbol used to display this task's completion status.
     *
     * @return {@code X} when completed, or a space when incomplete
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    @Override
    public String toString() {
        String details = switch (type) {
        case 'D' -> " (by: " + by + ")";
        case 'E' -> " (from: " + from + " to: " + to + ")";
        default -> "";
        };
        return "[" + type + "][" + getStatusIcon() + "] " + description + details;
    }
}
