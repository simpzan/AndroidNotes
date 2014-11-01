package simpzan.notes.domain;

import java.util.List;

/**
 * Created by guoqing.zgg on 2014/10/22.
 */
public interface INoteRepository {
    public Note createNote(Note note);
    public Note findNoteById(long id);
    public Note findNoteBy(String field, String value);
    public List<Note> findAllNotes();
    public Note updateNote(Note note);
    public void deleteNote(Note note);
}
