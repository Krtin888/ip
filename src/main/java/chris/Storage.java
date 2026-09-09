package chris;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Loads and saves tasks using an operating-system-independent path. */
public class Storage {
    private final Path filePath;

    /** Creates storage backed by the given relative file path. */
    public Storage(String filePath) {
        this.filePath = Path.of(filePath);
    }

    /** Loads all tasks, returning an empty list when the data file is absent. */
    public ArrayList<Task> load() throws ChrisException {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return tasks;
        }
        try {
            return Files.readAllLines(filePath).stream()
                    .map(this::parseTask)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException | RuntimeException exception) {
            throw new ChrisException("I could not read the saved tasks: " + exception.getMessage());
        }
    }

    /** Saves all tasks, creating the data directory when necessary. */
    public void save(List<Task> tasks) throws ChrisException {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(filePath, tasks.stream().map(Task::toDataString).toList());
        } catch (IOException exception) {
            throw new ChrisException("I could not save the tasks: " + exception.getMessage());
        }
    }

    private Task parseTask(String line) {
        String[] fields = line.split(" \\| ", -1);
        if (fields.length < 3) {
            throw new IllegalArgumentException("invalid task record");
        }
        Task task = switch (fields[0]) {
        case "T" -> new Todo(fields[2]);
        case "D" -> new Deadline(fields[2], fields[3]);
        case "E" -> new Event(fields[2], fields[3], fields[4]);
        default -> throw new IllegalArgumentException("unknown task type");
        };
        if ("1".equals(fields[1])) {
            task.markAsDone();
        }
        return task;
    }
}
