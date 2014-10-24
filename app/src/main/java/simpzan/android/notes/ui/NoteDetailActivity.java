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

import simpzan.android.notes.R;
import simpzan.android.notes.domain.Note;
import simpzan.android.notes.domain.NoteManager;

public class NoteDetailActivity extends BaseActivity {

    public static final String NOTE_ID = "note_id";
    private static final String TAG = NoteDetailActivity.class.getSimpleName();

    @Inject
    NoteManager noteManager;
    private Note note;
    private boolean noteChanged = false;
    private boolean keyboardShowing = false;

    private EditText titleView;
    private EditText contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        initData();
        initViews();
        updateViews();
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveNote();
    }

    private void saveNote() {
        if (!noteChanged) return;

        note.setTitle(titleView.getText().toString());
        note.setContent(contentView.getText().toString());
        note.setModified(new Date());
        noteManager.saveNote(note);
    }

    private void initData() {
        int notFound = -2;
        long note_id = getIntent().getLongExtra(NOTE_ID, notFound);
        if (note_id == notFound) {
            Log.w(TAG, "note id should be provided in intent.");
            return;
        }
        note = noteManager.findNoteById(note_id);
    }

    private void updateViews() {
        titleView.setText(note.getTitle());
        contentView.setText(note.getContent());
    }

    private void initViews() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged");
                if (keyboardShowing) noteChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        titleView = (EditText) findViewById(R.id.titleView);
        titleView.addTextChangedListener(textWatcher);
        contentView = (EditText) findViewById(R.id.contentView);
        contentView.addTextChangedListener(textWatcher);

        listenKeyboardShowHideEvent();
    }

    private void listenKeyboardShowHideEvent() {
        final View root = findViewById(R.id.root);
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = root.getRootView().getHeight() - root.getHeight();
                keyboardShowing = heightDiff > 100;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
