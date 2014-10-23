package simpzan.android.notes.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

import simpzan.android.notes.domain.INoteRepository;
import simpzan.android.notes.domain.Note;
import simpzan.android.notes.domain.NoteManager;
import simpzan.android.notes.db.NoteRepository;
import simpzan.android.notes.R;


public class NoteListActivity extends Activity {

    ListView noteListView;
    EditText quickInputView;

    NoteManager noteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        INoteRepository repo = new NoteRepository(this);
        noteManager = new NoteManager(repo);

        initViews();
        updateViews();
    }

    private void initViews() {
        noteListView = (ListView)findViewById(R.id.listView);
        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        quickInputView = (EditText)findViewById(R.id.editText);
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

    private void onEditTextEnterPressed() {
        String title = quickInputView.getText().toString();
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
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                textView = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                textView = (TextView)convertView;
            }
            Note note = notes.get(position);
            textView.setText(note.getTitle());
            return textView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note_list, menu);
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
