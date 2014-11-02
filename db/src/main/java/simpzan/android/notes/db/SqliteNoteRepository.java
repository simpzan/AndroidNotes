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

    private static final String DB_NAME = "Notes.sqlite";
    private static final String TABLE_NOTES = "notes";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MODIFIED = "modified";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";

    private static final String COLUMN_GUID = "guid";
    private static final String COLUMN_USN = "updateSequenceNumber";
    private static final String COLUMN_DELETED = "deleted";
    private static final String COLUMN_DIRTY = "dirty";

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
        cv.put(COLUMN_TITLE, note.getTitle());
        cv.put(COLUMN_CONTENT, note.getContent());
        cv.put(COLUMN_MODIFIED, note.getModified().getTime());
        cv.put(COLUMN_GUID, note.getGuid());
        cv.put(COLUMN_USN, note.getUpdateSequenceNumber());
        cv.put(COLUMN_DELETED, note.isDeleted());
        cv.put(COLUMN_DIRTY, note.isDirty());
        return cv;
    }

    @Override
    public Note findNoteById(long id) {
        return findNoteBy("id", String.valueOf(id));
    }

    @Override
    public Note findNoteBy(String field, String value) {
        Cursor cursor = getQueryCursor(field, value);
        if (cursor == null) return null;

        cursor.moveToFirst();
        Note note = createNoteFromCursor(cursor);
        cursor.close();
        return note;
    }

    private Cursor getQueryCursor(String field, String value) {
        String where = field + " = " + value;
        Cursor cursor = getReadableDatabase().query(TABLE_NOTES, null, where, null, null, null, null);
        if (cursor.getCount() > 0)  return cursor;

        cursor.close();
        return null;
    }

    @Override
    public List<Note> findNotesBy(String field, String value) {
        Cursor cursor = getQueryCursor(field, value);
        if (cursor == null)  return new ArrayList<Note>();

        List<Note> result = convert2Notes(cursor);
        cursor.close();
        return result;
    }

    @Override
    public List<Note> findAllNotes() {
        Cursor cursor = getReadableDatabase().query(TABLE_NOTES,
                null, null, null, null, null, COLUMN_MODIFIED + " desc");
        List<Note> notes = convert2Notes(cursor);
        cursor.close();
        return notes;
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
        int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        Date modified = new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_MODIFIED)));
        String title = getStringFromCursor(cursor, COLUMN_TITLE);
        String content = getStringFromCursor(cursor, COLUMN_CONTENT);

        String guid = getStringFromCursor(cursor, COLUMN_GUID);
        long usn = cursor.getInt(cursor.getColumnIndex(COLUMN_USN));
        boolean deleted = cursor.getInt(cursor.getColumnIndex(COLUMN_DELETED)) > 0;
        boolean dirty = cursor.getInt(cursor.getColumnIndex(COLUMN_DIRTY)) > 0;

        Note note = new Note(title);
        note.setId(id);
        note.setContent(content);
        note.setModified(modified);
        note.setGuid(guid);
        note.setUpdateSequenceNumber(usn);
        note.setDeleted(deleted);
        note.setDirty(dirty);
        return note;
    }

    private String getStringFromCursor(Cursor cursor, String field) {
        return cursor.getString(cursor.getColumnIndex(field));
    }

    @Override
    public Note updateNote(Note note) {
        ContentValues cv = serialize(note);
        getWritableDatabase().update(TABLE_NOTES, cv, COLUMN_ID + " = " + note.getId(), null);
        return note;
    }

    @Override
    public void deleteNote(Note note) {
        String where = COLUMN_ID + "=" + note.getId();
        getWritableDatabase().delete(TABLE_NOTES, where, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NOTES +
                "(" + COLUMN_ID + " integer primary key autoincrement, " +
                COLUMN_TITLE + " text not null, " +
                COLUMN_CONTENT + " text, " +
                COLUMN_MODIFIED + " integer" +
                COLUMN_GUID + " text, " +
                COLUMN_USN + " integer, " +
                COLUMN_DELETED + " integer, " +
                COLUMN_DIRTY + " integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException();
    }
}
