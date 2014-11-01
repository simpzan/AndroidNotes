package simpzan.android.notes.db;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import simpzan.notes.domain.INoteRepository;
import simpzan.notes.domain.Note;

/**
 * Created by guoqing.zgg on 2014/10/22.
 * Note repository backed by the Realm mobile database.
 */
public class RealmNoteRepository implements INoteRepository {
    private static final String TAG = RealmNoteRepository.class.getSimpleName();
    static private long nextId;
    private Realm realm;

    public RealmNoteRepository(Context context) {
        this(Realm.getInstance(context));
    }

    public RealmNoteRepository(Realm realm) {
        this.realm = realm;
        initIdGenerator();
    }
    private void initIdGenerator() {
        long max = realm.where(RealmNote.class).findAll().max("id").longValue();
        nextId = max + 1;
    }
    private long generateUniqueId() {
        return nextId++;
    }

    @Override
    public Note createNote(Note note) {
        note.setId(generateUniqueId());
        Log.d(TAG, "creating note:" + note);
        realm.beginTransaction();
        RealmNote noteObj = realm.createObject(RealmNote.class);
        mapNoteToRealmObject(note, noteObj);
        realm.commitTransaction();
        return note;
    }

    private void mapNoteToRealmObject(Note note, RealmNote noteObj) {
        noteObj.setTitle(note.getTitle());
        noteObj.setContent(note.getContent());
        noteObj.setModified(note.getModified().getTime());
        noteObj.setId(note.getId());
    }

    @Override
    public Note findNoteById(long id) {
        return findNoteBy("id", String.valueOf(id));
    }

    @Override
    public Note findNoteBy(String field, String value) {
        RealmQuery<RealmNote> query = findNoteRealmObjects("id", value);
        Note note = convertToNote(query.findFirst());
        Log.d(TAG, "findNoteById result:" + note);
        return note;
    }

    private RealmQuery<RealmNote> findNoteRealmObjects(String field, String value) {
        return realm.where(RealmNote.class).equalTo(field, value);
    }

    private List<Note> convertToNotes(RealmResults<RealmNote> results) {
        List<Note> notes = new ArrayList<Note>();
        for (RealmNote note : results) {
            notes.add(convertToNote(note));
        }
        return notes;
    }

    private Note convertToNote(RealmNote note) {
        if (note == null)  return null;

        Note result = new Note(note.getTitle());
        result.setModified(new Date(note.getModified()));
        result.setContent(note.getContent());
        result.setId(note.getId());
        return result;
    }

    @Override
    public List<Note> findAllNotes() {
        RealmQuery<RealmNote> query = realm.where(RealmNote.class);
        RealmResults<RealmNote> results = query.findAll();
        Log.d(TAG, "findAllNotes - found:" + results.size());
        RealmResults<RealmNote> sorted = results.sort("modified", RealmResults.SORT_ORDER_DECENDING);
        return convertToNotes(sorted);
    }

    @Override
    public Note updateNote(Note note) {
        Log.d(TAG, "updateNote: " + note);
        String value = String.valueOf(note.getId());
        RealmNote noteObj = findNoteRealmObjects("id", value).findFirst();
        realm.beginTransaction();
        mapNoteToRealmObject(note, noteObj);
        realm.commitTransaction();
        return note;
    }

    @Override
    public void deleteNote(Note note) {
        String value = String.valueOf(note.getId());
        RealmQuery<RealmNote> query = findNoteRealmObjects("id", value);
        RealmResults<RealmNote> results = query.findAll();
        if (results.size() == 0) {
            Log.e(TAG, "deleteNote - the note " + note.toString() + " not exist.");
            return;
        }

        if (results.size() > 1) {
            Log.e(TAG, "deleteNote - more than one note to delete:" + results.toString());
        }
        realm.beginTransaction();
        Log.d(TAG, "deleteNotes:" + results);
        results.removeAll(results);
        realm.commitTransaction();
    }
}
