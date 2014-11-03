package simpzan.android.notes.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import java.util.Date;

import javax.inject.Inject;

import simpzan.android.notes.AsyncNoteManager;
import simpzan.android.notes.R;
import simpzan.notes.domain.Note;
import simpzan.notes.domain.NoteManager;

public class NoteDetailActivity extends BaseActivity {

    public static final String NOTE_ID = "note_id";
    private static final String TAG = NoteDetailActivity.class.getSimpleName();

    @Inject
    AsyncNoteManager asyncNoteManager;
//    @Inject
//    NoteManager noteManager;

    private Note note;

    private EditText titleView;
    private EditText contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        initViews();
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveNote();
    }

    private void saveNote() {
        String title = titleView.getText().toString();
        String content = contentView.getText().toString();
        if (title.equals(note.getTitle()) && content.equals(note.getContent())) return;

        note.setTitle(title);
        note.setContent(content);
        note.setModified(new Date());
        note.setDirty(true);

        asyncNoteManager.saveNote(note, new AsyncNoteManager.CallBack<Note>() {
            @Override
            public void onSuccess(Note data) {
            }

            @Override
            public void onException(Exception exception) {
                makeToast("save note failed");
            }
        });
    }

    private void initData() {
        int notFound = -2;
        final long note_id = getIntent().getLongExtra(NOTE_ID, notFound);
        if (note_id == notFound) {
            Log.w(TAG, "note id should be provided in intent.");
            return;
        }
        asyncNoteManager.findNoteById(note_id, new AsyncNoteManager.CallBack<Note>() {
            @Override
            public void onSuccess(Note data) {
                note = data;
                updateViews();
            }

            @Override
            public void onException(Exception exception) {
                makeToast("note not found:" + note_id);
            }
        });
    }

    private void updateViews() {
        titleView.setText(note.getTitle());
        contentView.setText(note.getContent());
    }

    private void initViews() {
        titleView = (EditText) findViewById(R.id.titleView);
        contentView = (EditText) findViewById(R.id.contentView);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
