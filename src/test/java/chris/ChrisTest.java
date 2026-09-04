package chris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests the command-response interface shared with the GUI. */
public class ChrisTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    public void getResponse_addAndListTask_returnsExpectedResponses() {
        Chris chris = new Chris(temporaryDirectory.resolve("data/tasks.txt").toString());

        String addResponse = chris.getResponse("todo read book");
        String listResponse = chris.getResponse("list");

        assertTrue(addResponse.contains("I've added this task"));
        assertTrue(addResponse.contains("[T][ ] read book"));
        assertTrue(listResponse.contains("1.[T][ ] read book"));
    }

    @Test
    public void getResponse_bye_setsExitRequested() {
        Chris chris = new Chris(temporaryDirectory.resolve("data/tasks.txt").toString());

        String response = chris.getResponse("bye");

        assertEquals("Bye. Hope to see you again soon!", response.strip());
        assertTrue(chris.isExitRequested());
    }
}
