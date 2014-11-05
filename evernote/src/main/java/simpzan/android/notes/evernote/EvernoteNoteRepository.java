package simpzan.android.notes.evernote;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.SyncChunk;
import com.evernote.edam.notestore.SyncChunkFilter;
import com.evernote.edam.notestore.SyncState;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.edam.type.Notebook;
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
    private static final String TAG = EvernoteNoteRepository.class.getSimpleName();
    public static final String CONSUMER_KEY = "simpzan-9925";
    public static final String CONSUMER_SECRET = "60e4fa505ecb18b2";
    public static final String NOTE_PREFIX =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" +
            "<en-note>";
    public static final String NOTE_SUFFIX = "</en-note>";
    private static final String KEY_LAST_UPDATE_COUNT = "last_update_count";
    private static final String APP_NAME = "AndroidNotes";

    private final EvernoteSession evernoteSession;
    private final SharedPreferences preferences;
    private String notebookGuid;


    public EvernoteNoteRepository(Context context, EvernoteSession session) {
        this.evernoteSession = session;
        preferences = context.getApplicationContext().getSharedPreferences(TAG, 0);
    }

    private NoteStore.Client getNoteStore() {
        try {
            return evernoteSession.getClientFactory().createNoteStoreClient().getClient();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getAuthToken() {
        return evernoteSession.getAuthToken();
    }

    public Note createNote(Note note) {
        com.evernote.edam.type.Note enNote = convertToEvernote(note);
        Log.i(TAG, "creating: " + enNote);
        try {
            enNote = getNoteStore().createNote(getAuthToken(), enNote);
            checkUpdateCount(enNote.getUpdateSequenceNum());
            updateNoteWithEvernote(note, enNote);
            return note;
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateNoteWithEvernote(Note note, com.evernote.edam.type.Note enNote) {
        note.setGuid(enNote.getGuid());
        note.setUpdateSequenceNumber(enNote.getUpdateSequenceNum());
        note.setDirty(false);
    }

    private com.evernote.edam.type.Note convertToEvernote(Note note) {
        NoteAttributes attributes = new NoteAttributes();
        attributes.setSourceApplication(APP_NAME);

        com.evernote.edam.type.Note enNote = new com.evernote.edam.type.Note();
        enNote.setTitle(note.getTitle());
        // todo: content
        enNote.setContent(NOTE_PREFIX + NOTE_SUFFIX);
        enNote.setUpdated(note.getModified().getTime());
        enNote.setGuid(note.getGuid());
        enNote.setAttributes(attributes);
        return enNote;
    }

    private Note convertToDomainNote(com.evernote.edam.type.Note enNote)
            throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        Note note = new Note(enNote.getTitle());
        // todo: content
        String content = getNoteStore().getNoteContent(getAuthToken(), enNote.getGuid());
        Log.d(TAG, content);
        note.setModified(new Date(enNote.getUpdated()));
        note.setGuid(enNote.getGuid());
        note.setUpdateSequenceNumber(enNote.getUpdateSequenceNum());
        note.setDeleted(!enNote.isActive());
        note.setDirty(false);
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
    public List<Note> findNotesBy(String field, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Note> findAllNotes() {
        SyncState syncState;
        try {
            syncState = getNoteStore().getSyncState(getAuthToken());
            int thisUpdateCount = syncState.getUpdateCount();
            int lastUpdateCount = loadUpdateCount();
            if (thisUpdateCount > lastUpdateCount) {
                List<Note> notes = fetchRemoteChangedNotes(lastUpdateCount, thisUpdateCount);
                Log.i(TAG, "remote notes:" + notes);
                return notes;
            }
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<Note>();
    }

    private List<Note> fetchRemoteChangedNotes(int lastUpdateCount, int updateCount)
            throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        List<Note> result = new ArrayList<Note>();

        SyncChunk chunk;
        for (int usn = lastUpdateCount;
                usn < updateCount;
                usn = chunk.getChunkHighUSN(), updateCount = chunk.getUpdateCount()) {
            chunk = getNoteStore().getFilteredSyncChunk(getAuthToken(), usn, 100, getSyncChunkFilter());
            result.addAll(createDeletedNotes(chunk.getExpungedNotes()));
            result.addAll(convertToDomainNotes(chunk.getNotes()));
        }
        saveUpdateCount(updateCount);
        return result;
    }

    private int loadUpdateCount() {
        return preferences.getInt(KEY_LAST_UPDATE_COUNT, 0);
    }

    private void saveUpdateCount(int updateCount) {
        preferences.edit().putInt(KEY_LAST_UPDATE_COUNT, updateCount).apply();
    }

    private List<Note> convertToDomainNotes(List<com.evernote.edam.type.Note> notes)
            throws TException, EDAMUserException, EDAMSystemException, EDAMNotFoundException {
        List<Note> result = new ArrayList<Note>();
        if (notes == null)  return result;

        for (com.evernote.edam.type.Note enNote : notes) {
            if (!isNoteTracked(enNote)) continue;

            Note note = convertToDomainNote(enNote);
            result.add(note);
        }

        Log.i(TAG, "notes in chunk: " + notes.toString());
        Log.i(TAG, "notes return: " + result.toString());
        return result;
    }

    private boolean isNoteTracked(com.evernote.edam.type.Note enNote)
            throws TException, EDAMUserException, EDAMSystemException {
        if (notebookGuid == null) {
            Notebook notebook = getNoteStore().getDefaultNotebook(getAuthToken());
            notebookGuid = notebook.getGuid();
        }
        return notebookGuid.equals(enNote.getNotebookGuid())
                && APP_NAME.equals(enNote.getAttributes().getSourceApplication());
    }

    private List<Note> createDeletedNotes(List<String> expungedNotes) {
        List<Note> result = new ArrayList<Note>();
        if (expungedNotes == null)  return result;

        for (String guid : expungedNotes) {
            Note note = new Note("");
            note.setGuid(guid);
            note.setDeleted(true);
            note.setDirty(false);
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
        Log.i(TAG, "updating: " + enNote);
        try {
            enNote = getNoteStore().updateNote(getAuthToken(), enNote);
            checkUpdateCount(enNote.getUpdateSequenceNum());
            updateNoteWithEvernote(note, enNote);
            return note;
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteNote(Note note) {
        Log.i(TAG, "deleting: " + note);
        try {
            int usn = getNoteStore().deleteNote(getAuthToken(), note.getGuid());
            checkUpdateCount(usn);
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

    private void checkUpdateCount(int usn) {
        if (usn != loadUpdateCount() + 1) {
            Log.e(TAG, "out of sync with server!!");
            // todo: out of sync with server.
        } else {
            saveUpdateCount(usn + 1);
        }
    }

    public void clearData() {
        preferences.edit().clear().commit();
    }
}
