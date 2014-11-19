package simpzan.android.notes.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.Date;

import simpzan.android.notes.R;
import simpzan.notes.domain.Note;

public class NoteContentFragment extends Fragment implements NoteDetailActivity.NoteContentView {
    private static final String TAG = NoteContentFragment.class.getName();

    private EditText titleView;
    private EditText contentView;

    private Note note;

    public NoteContentFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_note_detail, container, false);
        titleView = (EditText)view.findViewById(R.id.titleView);
        contentView = (EditText)view.findViewById(R.id.contentView);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateViews();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        Log.e(TAG, "hidden changed:" + hidden + "  note:" + note);

        if (titleView == null)  return;
        if (hidden) {
            getNote();
        } else {
            setNote(note);
        }
    }

    private void updateViews() {
        Log.d(TAG, "update views with note:" + note);
        if (titleView == null || note == null)  return;

        titleView.setText(note.getTitle());
        contentView.setText(note.getPlainTextContent());
    }

    @Override
    public void setNote(Note note) {
        this.note = note;
        updateViews();
    }

    @Override
    public Note getNote() {
        if (note == null)   return null;
        String title = titleView.getText().toString();
        String content = contentView.getText().toString();
        note.setTitle(title);
        note.setContent(content);
        return note;
    }

    @Override
    public Note.NoteType getNoteType() {
        return Note.NoteType.PlainNote;
    }
}
