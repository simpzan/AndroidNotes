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
 */
public class SqliteNoteRepository extends SQLiteOpenHelper implements INoteRepository {

    private static final String TABLE_NOTES = "notes";
    private static final String ID = "id";
    private static final String MODIFIED = "modified";
    private static final String TITLE = "title";
    private static final String CONTENT = "content";
    private static final String DB_NAME = "Notes.sqlite";

    public SqliteNoteRepository(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public long createNote(Note note) {
        ContentValues cv = serialize(note);
        return getWritableDatabase().insert(TABLE_NOTES, null, cv);
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
        Cursor cursor = getReadableDatabase().query(TABLE_NOTES,
                null,
                ID + "=" + id,
                null,
                null,
                null,
                null,
                "1");
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
    public void updateNote(Note note) {
        ContentValues cv = serialize(note);
        getWritableDatabase().update(TABLE_NOTES, cv, ID + "=" + note.getId(), null);
    }

    @Override
    public void deleteNote(long id) {
        getWritableDatabase().delete(TABLE_NOTES, ID + "=" + id, null);
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
