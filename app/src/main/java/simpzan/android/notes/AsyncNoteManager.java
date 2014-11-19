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

    private static class MainThreadNotifier<T> {
        void notify(final T result, final CallBack<T> callBack) {
            if (callBack == null)  return;

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (result != null) callBack.onSuccess(result);
                    else callBack.onException(new Exception());
                }
            });
        }
    }

    private static Handler mainHandler = new Handler(Looper.getMainLooper());
    private static ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private NoteManager noteManager;

    public void saveNote(final Note note, final CallBack<Note> callBack) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                noteManager.saveNote(note);
                new MainThreadNotifier<Note>().notify(note, callBack);
            }
        });
    }

    public void findNoteById(final long id, final CallBack<Note> callBack) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final Note note = noteManager.findNoteById(id);
                new MainThreadNotifier<Note>().notify(note, callBack);
            }
        });
    }

    public void findAllNotes(final CallBack<List<Note>> callBack) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Note> notes = noteManager.findAllNotes();
                new MainThreadNotifier<List<Note>>().notify(notes, callBack);
            }
        });
    }

    public void findActiveNotes(final CallBack<List<Note>> callBack) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Note> notes = noteManager.findActiveNotes();
                new MainThreadNotifier<List<Note>>().notify(notes, callBack);
            }
        });
    }

    public void deleteNote(final Note note, final CallBack<Integer> callBack) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                noteManager.deleteNote(note);
                new MainThreadNotifier<Integer>().notify(1, callBack);
            }
        });
    }


}
