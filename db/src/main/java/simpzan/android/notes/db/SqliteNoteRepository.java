package simpzan.android.notes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import simpzan.notes.domain.INoteRepository;
import simpzan.notes.domain.Note;

/**
 * Created by guoqing.zgg on 2014/10/24.
 * Note repository backed by sqlite3.
 */
public class SqliteNoteRepository extends SQLiteOpenHelper implements INoteRepository {

    private static final String TABLE_NOTES = "notes";
    private static final String ID = "id";
    private static final String MODIFIED = "modified";
    private static final String TITLE = "title";
    private static final String CONTENT = "content";
    private static final String DB_NAME = "Notes.sqlite";
    // todo: notes fields for server note info.

    public SqliteNoteRepository(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public Note createNote(Note note) {
        ContentValues cv = serialize(note);
        long id = getWritableDatabase().insert(TABLE_NOTES, null, cv);
        note.setId(id);
        return note;
    }

    private ContentValues serialize(Note note) {
        ContentValues cv = new ContentValues();
        cv.put(TITLE, note.getTitle());
        cv.put(CONTENT, note.getContent());
        cv.put(MODIFIED, note.getModified().getTime());
        return cv;
    }

    @Override
    public Note findNoteById(long id) {
        return findNoteBy("id", String.valueOf(id));
    }

    @Override
    public Note findNoteBy(String field, String value) {
        String where = field + " = " + value;
        Cursor cursor = getReadableDatabase().query(TABLE_NOTES, null, where, null, null, null, null, "1");
        if (cursor.getCount() < 1)  return null;

        cursor.moveToFirst();
        return createNoteFromCursor(cursor);
    }

    @Override
    public List<Note> findAllNotes() {
        Cursor cursor = getReadableDatabase().query(TABLE_NOTES,
                null, null, null, null, null, MODIFIED + " desc");
        return convert2Notes(cursor);
    }

    private List<Note> convert2Notes(Cursor cursor) {
        List<Note> notes = new ArrayList<Note>();
        if (cursor.getCount() < 1)  return notes;

        while (cursor.moveToNext()) {
            notes.add(createNoteFromCursor(cursor));
        }
        return notes;
    }

    private Note createNoteFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(ID));
        Date modified = new Date(cursor.getLong(cursor.getColumnIndex(MODIFIED)));
        String title = cursor.getString(cursor.getColumnIndex(TITLE));
        String content = cursor.getString(cursor.getColumnIndex(CONTENT));

        Note note = new Note(title);
        note.setId(id);
        note.setContent(content);
        note.setModified(modified);
        return note;
    }

    @Override
    public Note updateNote(Note note) {
        ContentValues cv = serialize(note);
        getWritableDatabase().update(TABLE_NOTES, cv, ID + "=" + note.getId(), null);
        return note;
    }

    @Override
    public void deleteNote(Note note) {
        String where = ID + "=" + note.getId();
        getWritableDatabase().delete(TABLE_NOTES, where, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NOTES +
                "(" + ID + " integer primary key autoincrement, " +
                TITLE + " text, " +
                CONTENT + " text, " +
                MODIFIED + " integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException();
    }
}
