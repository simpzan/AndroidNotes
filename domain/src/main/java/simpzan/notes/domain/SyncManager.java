package simpzan.notes.domain;

import java.util.List;

/**
 * Created by guoqing.zgg on 2014/10/29.
 * manager for note synchronization.
 * namely it fetch remote changed notes and merge into local repo,
 * then upload local changed notes to remote server, and record update server note info locally.
 *
 * POLICY for conflict resolution:
 *              remote  local   resolution
 *  conflict 1: delete  update  keep local.
 *  conflict 2: update  delete  keep remote.
 *  conflict 3: update  update  keep both, convert local note to new state.
 */
public class SyncManager {
    INoteRepository localRepo;
    INoteRepository remoteRepo;

    public SyncManager(INoteRepository localRepo, INoteRepository remoteRepo) {
        this.localRepo = localRepo;
        this.remoteRepo = remoteRepo;
    }

    public void sync() {
        downloadAndMerge();
        upload();
    }

    private void upload() {
        List<Note> notes = localRepo.findAllNotes();
        for (Note note : notes) {
            if (note.isDeleted()) {    // local delete
                remoteRepo.deleteNote(note);
                localRepo.deleteNote(note);
            } else if (note.isNew()) {     // local new
                Note createdNote = remoteRepo.createNote(note);
                localRepo.updateNote(createdNote);
            } else if (note.isUpdated()) {    // local update
                Note updatedNote = remoteRepo.updateNote(note);
                localRepo.updateNote(updatedNote);
            }
        }
    }

    private void downloadAndMerge() {
        List<Note> remoteNotes = remoteRepo.findAllNotes();
        for (Note remoteNote : remoteNotes) {
            Note localNote = localRepo.findNoteBy("guid", remoteNote.getGuid());
            if (localNote == null) {    // remote new
                localRepo.createNote(remoteNote);
            } else if (!remoteNote.isDeleted()) {    // remote delete
                handleRemoteDelete(localNote, remoteNote);
            } else if (remoteNote.getUpdateSequenceNumber() > localNote.getUpdateSequenceNumber()) {    // remote update
                handleRemoteUpdate(localNote, remoteNote);
            }
        }
    }

    private void handleRemoteUpdate(Note localNote, Note remoteNote) {
        // conflict 2: remote update, local delete: preserve remote.
        if (localNote.isDeleted()) {
            localRepo.updateNote(remoteNote);
        }
        // conflict 3: remote update, local update: keep both. so turn local to new note, and save remote note in local repo.
        else if (localNote.isUpdated()) {
            localNote.setNew();
            localRepo.createNote(localNote);
            localRepo.updateNote(remoteNote);
        }
        else {
            localRepo.updateNote(remoteNote);
        }
    }

    private void handleRemoteDelete(Note localNote, Note remoteNote) {
        // conflict 1: remote delete, local update: preserve local. so just return.
        if (localNote.isUpdated()) return;

        localRepo.deleteNote(localNote);
    }
}
