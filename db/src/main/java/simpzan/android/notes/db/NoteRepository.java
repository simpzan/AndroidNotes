package simpzan.android.notes.db;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import simpzan.android.notes.domain.INoteRepository;
import simpzan.android.notes.domain.Note;

/**
 * Created by guoqing.zgg on 2014/10/22.
 */
public class NoteRepository implements INoteRepository {
    private static final String TAG = NoteRepository.class.getSimpleName();
    static private long nextId;
    private Realm realm;

    public NoteRepository(Context context) {
        this(Realm.getInstance(context));
    }

    public NoteRepository(Realm realm) {
        this.realm = realm;
        initIdGenerator();
    }
    private void initIdGenerator() {
        long max = realm.where(NoteRealmObject.class).findAll().max("id").longValue();
        nextId = max + 1;
    }
    private long generateUniqueId() {
        return nextId++;
    }

    @Override
    public long createNote(Note note) {
        note.setId(generateUniqueId());
        Log.d(TAG, "creating note:" + note);
        realm.beginTransaction();
        NoteRealmObject noteObj = realm.createObject(NoteRealmObject.class);
        mapNoteToRealmObject(note, noteObj);
        realm.commitTransaction();
        return 0;
    }

    private void mapNoteToRealmObject(Note note, NoteRealmObject noteObj) {
        noteObj.setTitle(note.getTitle());
        noteObj.setContent(note.getContent());
        noteObj.setModified(note.getModified().getTime());
        noteObj.setId(note.getId());
    }

    @Override
    public Note findNoteById(long id) {
        RealmQuery<NoteRealmObject> query = findNoteRealmObjects("id", id);
        Note note = convertToNote(query.findFirst());
        Log.d(TAG, "findNoteById result:" + note);
        return note;
    }

    private RealmQuery<NoteRealmObject> findNoteRealmObjects(String field, long id) {
        return realm.where(NoteRealmObject.class).equalTo(field, id);
    }

    private List<Note> convertToNotes(RealmResults<NoteRealmObject> results) {
        List<Note> notes = new ArrayList<Note>();
        for (NoteRealmObject note : results) {
            notes.add(convertToNote(note));
        }
        return notes;
    }

    private Note convertToNote(NoteRealmObject note) {
        if (note == null)  return null;

        Note result = new Note(note.getTitle());
        result.setModified(new Date(note.getModified()));
        result.setContent(note.getContent());
        result.setId(note.getId());
        return result;
    }

    @Override
    public List<Note> findAllNotes() {
        RealmQuery<NoteRealmObject> query = realm.where(NoteRealmObject.class);
        RealmResults<NoteRealmObject> results = query.findAll();
        Log.d(TAG, "findAllNotes - found:" + results.size());
        RealmResults<NoteRealmObject> sorted = results.sort("modified", RealmResults.SORT_ORDER_DECENDING);
        return convertToNotes(sorted);
    }

    @Override
    public void updateNote(Note note) {
        Log.d(TAG, "updateNote: " + note);
        NoteRealmObject noteObj = findNoteRealmObjects("id", note.getId()).findFirst();
        realm.beginTransaction();
        mapNoteToRealmObject(note, noteObj);
        realm.commitTransaction();
    }

    @Override
    public void deleteNote(long id) {
        RealmQuery<NoteRealmObject> query = findNoteRealmObjects("id", id);
        RealmResults<NoteRealmObject> results = query.findAll();
        if (results.size() == 0) {
            Log.e(TAG, "deleteNote - the note with id " + id + " not exist.");
            return;
        }

        if (results.size() > 1) {
            Log.e(TAG, "deleteNote - more than one note to delete:" + results.toString());
        }
        realm.beginTransaction();
        Log.d(TAG, "deleteNote:" + results.get(0));
        results.remove(results.get(0));
        realm.commitTransaction();
    }
}
