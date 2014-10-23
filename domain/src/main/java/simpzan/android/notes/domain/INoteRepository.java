package simpzan.android.notes.domain;

import java.util.List;

/**
 * Created by guoqing.zgg on 2014/10/22.
 */
public interface INoteRepository {
    public long createNote(Note note);
    public Note findNoteById(long id);
    public List<Note> findAllNotes();
    public void updateNote(Note note);
    public void deleteNote(long id);
}