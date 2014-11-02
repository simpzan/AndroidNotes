package simpzan.notes.domain;

import java.util.List;
import java.util.logging.Logger;

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
    private final static Logger LOGGER = Logger.getLogger(SyncManager.class.getName());

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
        LOGGER.info("uploading start");

        List<Note> notes = localRepo.findNotesBy("dirty", "1");
        for (Note note : notes) {
            LOGGER.info("uploading local note; " + note.toString());

            if (note.isDeleted()) {    // local delete
                remoteRepo.deleteNote(note);
                localRepo.deleteNote(note);
            } else if (note.isNew()) {     // local new
                Note createdNote = remoteRepo.createNote(note);
                if (createdNote != null)  localRepo.updateNote(createdNote);
            } else if (note.isUpdated()) {    // local update
                Note updatedNote = remoteRepo.updateNote(note);
                if (updatedNote != null)  localRepo.updateNote(updatedNote);
            }
        }

        LOGGER.info("uploading end");
    }

    private void downloadAndMerge() {
        LOGGER.info("download start");
        List<Note> remoteNotes = remoteRepo.findAllNotes();
        LOGGER.info("downloaded and merging remote notes; " + remoteNotes);
        for (Note remoteNote : remoteNotes) {
            Note localNote = localRepo.findNoteBy("guid", "\"" + remoteNote.getGuid() +"\"");
            if (remoteNote.isDeleted()) {    // remote delete
                handleRemoteDelete(localNote, remoteNote);
            } else if (localNote == null) {    // remote new
                localRepo.createNote(remoteNote);
            } else if (remoteNote.getUpdateSequenceNumber() > localNote.getUpdateSequenceNumber()) {    // remote update
                handleRemoteUpdate(localNote, remoteNote);
            }
        }
        LOGGER.info("merge end");
    }

    private void handleRemoteUpdate(Note localNote, Note remoteNote) {
        remoteNote.setId(localNote.getId());
        // conflict 2: remote update, local delete: preserve remote.
        if (localNote.isDeleted()) {
            localRepo.updateNote(remoteNote);
        }
        // conflict 3: remote update, local update: keep both. so turn local to new note, and save remote note in local repo.
        else if (localNote.isUpdated()) {
            remoteNote.setTitle(remoteNote.getTitle() + "-remote");
            localRepo.updateNote(remoteNote);

            localNote.setNew();
            localNote.setTitle(localNote.getTitle() + "-local");
            localRepo.createNote(localNote);
        }
        else {
            localRepo.updateNote(remoteNote);
        }
    }

    private void handleRemoteDelete(Note localNote, Note remoteNote) {
        if (localNote == null)  return;
        // conflict 1: remote delete, local update: preserve local. so just return.
        if (localNote.isUpdated()) return;

        localRepo.deleteNote(localNote);
    }
}
