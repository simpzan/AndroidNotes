package simpzan.android.notes;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class NoteTest {
    @Test
    public void createNoteTest() {
        String title = "title_test";
        Date now = new Date();

        Note note = new Note(title);
        Assert.assertEquals(note.getTitle(), title);
        assertDatesAlmostEqual(note.getModified(), now);
    }

    private void assertDatesAlmostEqual(Date expected, Date actual) {
        long diff = expected.getTime() - actual.getTime();
        Assert.assertTrue(Math.abs(diff) < 10);
    }
}