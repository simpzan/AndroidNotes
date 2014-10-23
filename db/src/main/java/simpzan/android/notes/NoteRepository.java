package simpzan.android.notes;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by guoqing.zgg on 2014/10/22.
 */
public class NoteRepository implements INoteRepository {
    private Realm realm;

    public NoteRepository(Context context) {
        this.realm = Realm.getInstance(context);
    }

    public NoteRepository(Realm realm) { this.realm = realm; }

    @Override
    public long createNote(Note note) {
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
        return convertToNote(query.findFirst());
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
        return convertToNotes(results);
    }

    @Override
    public void updateNote(Note note) {
        createNote(note);
    }

    @Override
    public void deleteNote(long id) {
        realm.beginTransaction();
        RealmQuery<NoteRealmObject> query = findNoteRealmObjects("id", id);
        query.findAll().removeLast();
        realm.commitTransaction();
    }
}
