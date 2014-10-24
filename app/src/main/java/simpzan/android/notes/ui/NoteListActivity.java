package simpzan.android.notes.ui;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.List;

import javax.inject.Inject;

import simpzan.android.notes.R;
import simpzan.android.notes.domain.Note;
import simpzan.android.notes.domain.NoteManager;


public class NoteListActivity extends BaseActivity {

    ListView noteListView;
    EditText quickInputView;

    @Inject
    NoteManager noteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        initViews();
        updateViews();
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
        noteManager.saveNote(note);

        updateViews();
        quickInputView.setText("");
    }

    private void updateViews() {
        final List<Note> notes = noteManager.findAllNotes();
        ListAdapter adapter = new NoteListAdapter(notes);
        noteListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            TextView textView = null;
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
