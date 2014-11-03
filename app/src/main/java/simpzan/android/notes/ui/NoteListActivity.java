package simpzan.android.notes.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.inject.Inject;

import simpzan.android.notes.AsyncNoteManager;
import simpzan.android.notes.R;
import simpzan.android.notes.evernote.EvernoteNoteRepository;
import simpzan.notes.domain.Note;
import simpzan.notes.domain.SyncManager;


public class NoteListActivity extends BaseActivity {

    private static final String TAG = NoteListActivity.class.getSimpleName();
    ListView noteListView;
    EditText quickInputView;

    @Inject
    EvernoteSession evernoteSession;
    @Inject
    EvernoteNoteRepository evernoteNoteRepository;
    @Inject
    SyncManager syncManager;

    @Inject
    AsyncNoteManager asyncNoteManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        initViews();
    }

    private void test() {
        Logger logger = Logger.getLogger(NoteListActivity.class.getName());
//        logger.setLevel(Level.FINEST);
        Locale locale = Locale.getDefault();
        logger.info("default locale:" + locale);
        Log.d(TAG, "default locale:" + locale);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
    }

    private void initViews() {
        noteListView = (ListView) findViewById(R.id.listView);
        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = (Note) noteListView.getItemAtPosition(position);
                openNoteDetailActivity(note);
            }
        });
        quickInputView = (EditText) findViewById(R.id.editText);
        quickInputView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        onEditTextEnterPressed();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void openNoteDetailActivity(Note note) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra(NoteDetailActivity.NOTE_ID, note.getId());
        startActivity(intent);
    }

    private void onEditTextEnterPressed() {
        String title = quickInputView.getText().toString();
        if (title.length() == 0) return;

        Note note = new Note(title);
        asyncNoteManager.saveNote(note, new AsyncNoteManager.CallBack<Note>() {
            @Override
            public void onSuccess(Note data) {
                updateViews();
                quickInputView.setText("");
            }

            @Override
            public void onException(Exception exception) {
                makeToast("save note failed");
            }
        });
    }

    private void updateViews() {
        asyncNoteManager.findAllNotes(new AsyncNoteManager.CallBack<List<Note>>() {
            @Override
            public void onSuccess(List<Note> notes) {
                ListAdapter adapter = new NoteListAdapter(notes);
                noteListView.setAdapter(adapter);
            }

            @Override
            public void onException(Exception exception) {
                makeToast("find notes failed");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_full_sync) {
            fullSync();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_sync) {
            incrementalSync();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fullSync() {
        evernoteNoteRepository.clearData();
        syncWithEvernote();
    }

    private void logout() {
        try {
            evernoteSession.logOut(this);
            evernoteNoteRepository.clearData();
        } catch (InvalidAuthenticationException e) {
            e.printStackTrace();
        }
    }

    private void incrementalSync() {
        if (!evernoteSession.isLoggedIn()) {
            evernoteSession.authenticate(this);
        } else {
            syncWithEvernote();
        }
    }

    private void syncWithEvernote() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.w(TAG, "syncWithEvernote start");
                syncManager.sync();
                Log.w(TAG, "syncWithEvernote done");
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                updateViews();
                makeToast("Syncing done");
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EvernoteSession.REQUEST_CODE_OAUTH && resultCode == Activity.RESULT_OK) {
            syncWithEvernote();
        }
    }

    private class NoteListAdapter extends BaseAdapter {
        private final List<Note> notes;

        NoteListAdapter(List<Note> notes) {
            this.notes = notes;
        }

        @Override
        public int getCount() {
            return notes.size();
        }

        @Override
        public Object getItem(int position) {
            return notes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                textView = (TextView) convertView;
            }
            Note note = notes.get(position);
            textView.setText(note.getTitle());
            return textView;
        }
    }
}
