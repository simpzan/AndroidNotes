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
    private ListView noteListView;
    private EditText quickInputView;

    private Menu menu;
    private NoteListAdapter listAdapter;

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
        addSwipeDeleteSupport();

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

    private void addSwipeDeleteSupport() {
        SwipeDismissListViewTouchListener swipeListener = new SwipeDismissListViewTouchListener(noteListView,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {

                    @Override public boolean canDismiss(int position) {   return true;    }

                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        int pos = reverseSortedPositions[0];
                        Note note = (Note) noteListView.getItemAtPosition(pos);
                        deleteNote(note);
                    }
                }
        );
        noteListView.setOnTouchListener(swipeListener);
        noteListView.setOnScrollListener(swipeListener.makeScrollListener());
    }

    private void deleteNote(Note note) {
        // hack: remove the note in RAM right away to avoid UI flash caused by SwipeDismissListView.
        listAdapter.removeNote(note);
        listAdapter.notifyDataSetChanged();

        note.setDeleted(true);
        asyncNoteManager.saveNote(note, new AsyncNoteManager.CallBack<Note>() {
            @Override
            public void onSuccess(Note data) {
                Log.d(TAG, "note mark deleted:" + data);
                makeToast("note deleted: " + data.getTitle());
//                updateViews();    // the note is already removed in RAM and UI, so no need to refresh UI.
            }

            @Override public void onException(Exception exception) {
                makeToast("note delete failed");
                updateViews();  // note delete failed. the note should be add to RAM and UI. To fulfill this, we reload from db.
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
        asyncNoteManager.findActiveNotes(new AsyncNoteManager.CallBack<List<Note>>() {
            @Override
            public void onSuccess(List<Note> notes) {
                listAdapter = new NoteListAdapter(notes);
                noteListView.setAdapter(listAdapter);
            }

            @Override
            public void onException(Exception exception) {
                makeToast("find notes failed");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu aMenu) {
        getMenuInflater().inflate(R.menu.note_list, aMenu);
        menu = aMenu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_full_sync) {
            fullSync();
        } else if (id == R.id.action_sync) {
            incrementalSync();
        } else if (id == R.id.action_logout) {
            logout();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
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
        updateMenuForSyncBegin();
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
                updateMenuForSyncEnd();
            }
        }.execute();
    }

    private void updateMenuForSyncEnd() {
        MenuItem item = menu.findItem(R.id.action_sync);
        item.setTitle(R.string.action_sync);
        item.setEnabled(true);
    }

    private void updateMenuForSyncBegin() {
        MenuItem item = menu.findItem(R.id.action_sync);
        item.setTitle(R.string.syncing);
        item.setEnabled(false);
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

        NoteListAdapter(List<Note> notes) { this.notes = notes; }

        public void removeNote(Note note) { notes.remove(note); }

        @Override public int getCount() { return notes.size(); }

        @Override public Object getItem(int position) { return notes.get(position); }

        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = (TextView) convertView;
            if (textView == null) {
                textView = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            Note note = notes.get(position);
            textView.setText(note.getTitle());
            return textView;
        }
    }
}
