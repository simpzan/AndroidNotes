package simpzan.android.notes.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import javax.inject.Inject;

import simpzan.android.notes.AsyncNoteManager;
import simpzan.android.notes.R;
import simpzan.notes.domain.Note;

public class NoteDetailActivity extends BaseActivity {

    public interface NoteContentView {
        public void setNote(Note note);
        public Note getNote();
        public Note.NoteType getNoteType();
    }

    public static final String NOTE_ID = "note_id";
    private static final String TAG = NoteDetailActivity.class.getName();
    private NoteContentFragment noteContentFragment;
    private TodoNoteContentFragment todoNoteContentFragment;
    private Note noteBackup;
    private Note noteEditing;
    private boolean noteSaved;

    @Inject
    AsyncNoteManager asyncNoteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

//        getActionBar().setDisplayHomeAsUpEnabled(true);
        loadNote();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNote();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_hide_checkbox);
        menuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_note) {
            deleteNote();
        } else if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_show_checkbox || id == R.id.action_hide_checkbox) {
            switchFragment();
            return true;
        } else if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_rename_title) {
            promptRenameNoteTitle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void promptRenameNoteTitle() {
        final EditText input = new EditText(this);
        input.setText(noteEditing.getTitle());

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.action_rename_title));
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                noteEditing.setTitle(value);
                setTitle(value);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    private void setupFragment() {
        Note aNote = new Note(noteBackup);
        noteEditing = aNote;
        noteContentFragment = new NoteContentFragment();
        noteContentFragment.setNote(aNote);
        todoNoteContentFragment = new TodoNoteContentFragment();
        todoNoteContentFragment.setNote(aNote);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.container, noteContentFragment, null);
        transaction.add(R.id.container, todoNoteContentFragment, null);
        Fragment fragment = aNote.getContentType() == Note.NoteType.PlainNote ? todoNoteContentFragment : noteContentFragment;
        transaction.hide(fragment);
        transaction.commit();
    }

    private void switchFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (noteContentFragment.isHidden()) {
            transaction.hide(todoNoteContentFragment);
            transaction.show(noteContentFragment);
        } else {
            transaction.hide(noteContentFragment);
            transaction.show(todoNoteContentFragment);
        }
        transaction.commit();
    }

    private void deleteNote() {
        noteBackup.setDeleted(true);
        asyncNoteManager.saveNote(noteBackup, new AsyncNoteManager.CallBack<Note>() {
            @Override
            public void onSuccess(Note data) {
                Log.d(TAG, "note mark deleted:" + data);
                makeToast("note deleted");
                noteSaved = true;
                finish();
            }

            @Override
            public void onException(Exception exception) {
                makeToast("note delete failed");
            }
        });
    }

    private void loadNote() {
        final int notFound = -2;
        final long note_id = getIntent().getLongExtra(NOTE_ID, notFound);
        if (note_id == notFound) {
            Log.w(TAG, "note id should be provided in intent.");
            return;
        }

        Log.d(TAG, "load note");
        asyncNoteManager.findNoteById(note_id, new AsyncNoteManager.CallBack<Note>() {
            @Override
            public void onSuccess(Note aNote) {
                noteBackup = aNote;
                setTitle(noteBackup.getTitle());
                setupFragment();
            }

            @Override
            public void onException(Exception exception) {
                makeToast("note not found:" + note_id);
            }
        });
    }

    private void saveNote() {
        if (noteContentFragment == null || noteSaved) return;

        final Note noteAfter = noteContentFragment.isHidden() ? todoNoteContentFragment.getNote() : noteContentFragment.getNote();
        if (noteBackup.equals(noteAfter)) return;

        asyncNoteManager.saveNote(noteAfter, new AsyncNoteManager.CallBack<Note>() {
            @Override
            public void onSuccess(Note data) {
                makeToast("note saved!");
                Log.v(TAG, "before:" + noteBackup + "\nafter:" + data);
            }

            @Override
            public void onException(Exception exception) {
                makeToast("save note failed");
            }
        });
    }
}
