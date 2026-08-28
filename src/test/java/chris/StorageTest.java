package chris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests saving and loading task data. */
public class StorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    public void saveAndLoad_multipleTaskTypes_preservesTaskData() throws ChrisException {
        Storage storage = new Storage(temporaryDirectory.resolve("data/tasks.txt").toString());
        ArrayList<Task> tasks = new ArrayList<>();
        Todo todo = new Todo("read book");
        todo.markAsDone();
        tasks.add(todo);
        tasks.add(new Deadline("return book", "2026-12-02 1800"));

        storage.save(tasks);
        ArrayList<Task> loadedTasks = storage.load();

        assertEquals(2, loadedTasks.size());
        assertEquals(tasks.get(0).toString(), loadedTasks.get(0).toString());
        assertEquals(tasks.get(1).toString(), loadedTasks.get(1).toString());
    }

    @Test
    public void load_missingFile_returnsEmptyList() throws ChrisException {
        Storage storage = new Storage(temporaryDirectory.resolve("missing/tasks.txt").toString());

        assertTrue(storage.load().isEmpty());
    }
}
