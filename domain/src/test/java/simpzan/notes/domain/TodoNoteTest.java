package simpzan.notes.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TodoNoteTest {

    private TodoNoteMapper todoNote;
    private TodoNoteMapper.TodoItem[] todoItems;

    @Before
    public void setUp() throws Exception {
        todoNote = new TodoNoteMapper();
        TodoNoteMapper.TodoItem[] items = {
                new TodoNoteMapper.TodoItem(false, "first line"),
                new TodoNoteMapper.TodoItem(true, "end line"),
                new TodoNoteMapper.TodoItem(false, "last line"),
        };
        todoItems = items;
    }

    @Test
    public void testDeserialze() throws Exception {
        String todoString = "- [ ] first line\n- [x] end line\nlast line\n";
        List<TodoNoteMapper.TodoItem> items = todoNote.deserialze(todoString);
        Assert.assertArrayEquals(todoItems, items.toArray());
    }

    @Test
    public void testSerialize() throws Exception {
        String serizlied = todoNote.serialize(Arrays.asList(todoItems));
        String expected = "- [ ] first line\n- [x] end line\n- [ ] last line\n";
        Assert.assertEquals(expected, serizlied);
    }
}