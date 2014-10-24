package simpzan.android.notes.db;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.List;

import io.realm.Realm;
import simpzan.android.notes.domain.Note;

public class RealmNoteRepositoryTest extends AndroidTestCase {

    Note expectedNote;
    RealmNoteRepository repo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        expectedNote = new Note("title_test");
        Realm realm = Realm.getInstance(getContext(), toString());
        repo = new RealmNoteRepository(realm);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateNote() throws Exception {
        repo.createNote(expectedNote);
        Note actualNote = repo.findNoteById(expectedNote.getId());
        Assert.assertEquals(actualNote, expectedNote);
    }

    public void testFindNoteById() throws Exception {
        Note actualNote = repo.findNoteById(expectedNote.getId());
        Assert.assertNull(actualNote);
        testCreateNote();
    }

    public void testFindAllNotes() throws Exception {
        List<Note> notes = repo.findAllNotes();
        Assert.assertTrue(notes.size() == 0);
        repo.createNote(expectedNote);
        notes = repo.findAllNotes();
        Assert.assertTrue(notes.size() == 1);
        Assert.assertEquals(expectedNote, notes.get(0));
    }

    public void testUpdateNote() throws Exception {
        repo.createNote(expectedNote);
        expectedNote.setContent("content_test");
        expectedNote.setId(1112);
        repo.updateNote(expectedNote);
        Note actualNote = repo.findNoteById(expectedNote.getId());
        Assert.assertEquals(actualNote, expectedNote);
    }

    public void testDeleteNote() throws Exception {
        testCreateNote();
        repo.deleteNote(expectedNote.getId());
        Note actualNote = repo.findNoteById(expectedNote.getId());
        Assert.assertNull(actualNote);
    }
}