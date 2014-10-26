package simpzan.android.notes;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import simpzan.notes.domain.Note;
import simpzan.notes.domain.NoteManager;

/**
 * Created by simpzan on 2014/10/26.
 */
public class AsyncNoteManager {

    public AsyncNoteManager(NoteManager noteManager) {
        this.noteManager = noteManager;
    }

    public static interface CallBack<T> {
        public void onSuccess(final T data);
        public void onException(final Exception exception);
    }

    private static Handler mainHandler = new Handler(Looper.getMainLooper());
    private static ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private NoteManager noteManager;

    public void saveNote(final Note note, final CallBack<Note> callBack) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                noteManager.saveNote(note);
                if (callBack == null)  return;

                final boolean successful = true;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (note != null) callBack.onSuccess(note);
                        else callBack.onException(new Exception());
                    }
                });
            }
        });
    }

    public void findNoteById(final long id, final CallBack<Note> callBack) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final Note note = noteManager.findNoteById(id);
                if (callBack == null)  return;

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (note != null) callBack.onSuccess(note);
                        else callBack.onException(new Exception());
                    }
                });
            }
        });
    }

    public void findAllNotes(final CallBack<List<Note>> callBack) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Note> notes = noteManager.findAllNotes();
                if (callBack == null)  return;

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (notes != null) callBack.onSuccess(notes);
                        else callBack.onException(new Exception());
                    }
                });
            }
        });
    }

    public void deleteNote(final long id, final CallBack<Integer> callBack) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                noteManager.deleteNote(id);
                if (callBack == null)  return;

                final boolean successful = true;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (successful) callBack.onSuccess(1);
                        else callBack.onException(new Exception());
                    }
                });
            }
        });
    }
}
