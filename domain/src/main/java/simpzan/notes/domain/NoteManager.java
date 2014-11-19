package simpzan.notes.domain;

import java.util.Date;
import java.util.List;

/**
 * Created by guoqing.zgg on 2014/10/22.
 */
public class NoteManager {
    private INoteRepository localRepo;

    public NoteManager(INoteRepository localRepo) {
        this.localRepo = localRepo;
    }

    public void saveNote(Note note) {
        note.setModified(new Date());
        note.setDirty(true);
        if (note.getId() == 0) {
            localRepo.createNote(note);
        } else {
            localRepo.updateNote(note);
        }
    }

    public List<Note> findAllNotes() {
        return localRepo.findAllNotes();
    }

    public List<Note> findActiveNotes() {
        return localRepo.findNotesBy("deleted", "0");
    }

    public Note findNoteById(long id) {
        return localRepo.findNoteById(id);
    }

    public void deleteNote(Note note) {
        localRepo.deleteNote(note);
    }
}
