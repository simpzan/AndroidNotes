package simpzan.android.notes.evernote;

import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.SyncChunk;
import com.evernote.edam.notestore.SyncChunkFilter;
import com.evernote.edam.notestore.SyncState;
import com.evernote.thrift.TException;
import com.evernote.thrift.transport.TTransportException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import simpzan.notes.domain.INoteRepository;
import simpzan.notes.domain.Note;

/**
 * Created by guoqing.zgg on 2014/11/1.
 * Note repository for evernote web service api.
 */
public class EvernoteNoteRepository implements INoteRepository {
    private final EvernoteSession evernoteSession;
    private final String authenticateToken;
    private List<String> tagNames = new ArrayList<String>();

    public EvernoteNoteRepository(EvernoteSession session) {
        this.evernoteSession = session;
        this.authenticateToken = evernoteSession.getAuthToken();
        tagNames.add("AndroidNotes");
        tagNames.add("simpzan");
    }

    private NoteStore.Client getNoteStore() {
        try {
            return evernoteSession.getClientFactory().createNoteStoreClient().getClient();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Note createNote(Note note) {
        com.evernote.edam.type.Note enNote = convertToEvernote(note);
        try {
            enNote = getNoteStore().createNote(authenticateToken, enNote);
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return convertToDomainNote(enNote);
    }

    private com.evernote.edam.type.Note convertToEvernote(Note note) {
        com.evernote.edam.type.Note enNote = new com.evernote.edam.type.Note();
        enNote.setTitle(note.getTitle());
        // todo: content
        enNote.setUpdated(note.getModified().getTime());
        enNote.setGuid(note.getGuid());
        enNote.setTagNames(tagNames);
        return enNote;
    }

    private Note convertToDomainNote(com.evernote.edam.type.Note enNote) {
        Note note = new Note(enNote.getTitle());
        // todo: content
        note.setModified(new Date(enNote.getUpdated()));
        note.setGuid(enNote.getGuid());
        note.setUpdateSequenceNumber(enNote.getUpdateSequenceNum());
        note.setDeleted(!enNote.isActive());
        return note;
    }

    @Override
    public Note findNoteById(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Note findNoteBy(String field, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Note> findAllNotes() {
        SyncState syncState;
        try {
            syncState = getNoteStore().getSyncState(authenticateToken);
            int lastUpdateCount = 0; // todo:
            int thisUpdateCount = syncState.getUpdateCount();

            if (thisUpdateCount > lastUpdateCount) {
                return fetchRemoteChangedNotes(lastUpdateCount, thisUpdateCount);
            }
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return new ArrayList<Note>();
    }

    private List<Note> fetchRemoteChangedNotes(int lastUpdateSequence, int updateCount)
            throws EDAMUserException, EDAMSystemException, TException {
        List<Note> result = new ArrayList<Note>();

        SyncChunk chunk;
        for (int usn = lastUpdateSequence;
                usn < updateCount;
                usn = chunk.getChunkHighUSN(), updateCount = chunk.getUpdateCount()) {
            chunk = getNoteStore().getFilteredSyncChunk(authenticateToken, usn, 100, getSyncChunkFilter());
            result.addAll(createDeletedNotes(chunk.getExpungedNotes()));
            result.addAll(convertToDomainNotes(chunk.getNotes()));
        }
        // todo: when to save thisUpdateSequence?
        return result;
    }

    private List<Note> convertToDomainNotes(List<com.evernote.edam.type.Note> notes) {
        List<Note> result = new ArrayList<Note>();
        for (com.evernote.edam.type.Note enNote : notes) {
            Note note = convertToDomainNote(enNote);
            result.add(note);
        }
        return result;
    }

    private List<Note> createDeletedNotes(List<String> expungedNotes) {
        List<Note> result = new ArrayList<Note>();
        if (expungedNotes == null)  return result;

        for (String guid : expungedNotes) {
            Note note = new Note("");
            note.setGuid(guid);
            note.setDeleted(true);
            result.add(note);
        }
        return result;
    }

    private SyncChunkFilter getSyncChunkFilter() {
        SyncChunkFilter filter = new SyncChunkFilter();
        filter.setIncludeNotes(true);
        filter.setIncludeExpunged(true);
        filter.setIncludeNoteAttributes(true);
        return filter;
    }

    @Override
    public Note updateNote(Note note) {
        com.evernote.edam.type.Note enNote = convertToEvernote(note);
        try {
            enNote = getNoteStore().updateNote(authenticateToken, enNote);
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return convertToDomainNote(enNote);
    }

    @Override
    public void deleteNote(Note note) {
        try {
            getNoteStore().deleteNote(authenticateToken, note.getGuid());
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
    }
}
