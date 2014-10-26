package simpzan.notes.domain;

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
        if (note.getId() == 0) {
            localRepo.createNote(note);
        } else {
            localRepo.updateNote(note);
        }
    }

    public List<Note> findAllNotes() {
        return localRepo.findAllNotes();
    }

    public Note findNoteById(long id) {
        return localRepo.findNoteById(id);
    }

    public void deleteNote(long id) {
        localRepo.deleteNote(id);
    }
}
