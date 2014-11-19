package simpzan.android.notes.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import simpzan.android.notes.R;
import simpzan.notes.domain.Note;
import simpzan.notes.domain.TodoNoteMapper;


public class TodoNoteContentFragment extends Fragment implements NoteDetailActivity.NoteContentView {
    private static final String TAG = TodoNoteContentFragment.class.getName();
    private final int DRAG_HIGHLIGHT_COLOR = android.R.color.holo_red_dark;
    private final int DRAG_NORMAL_COLOR = android.R.color.darker_gray;
    private Note note;
    private List<TodoNoteMapper.TodoItem> todoItems;
    private ScrollView scrollView;
    private LinearLayout containerView;

    public TodoNoteContentFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo_list, container, false);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        containerView = (LinearLayout) view.findViewById(R.id.container);
        updateViews();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e(TAG, "hidden changed:" + hidden + "  note:" + note);

        if (containerView == null)  return;

        if (hidden) {
            getNote();
        } else {
            setNote(note);
        }
    }

    private void updateViews() {
        if (containerView == null || todoItems == null)  return;

        containerView.removeAllViews();

        View quickInputView = getQuickInputView();
        containerView.addView(quickInputView);

        for (TodoNoteMapper.TodoItem item : todoItems) {
            View view = getViewForRow(item);
            Log.e(TAG, "add view:" + item);
            containerView.addView(view);
        }
        Log.d(TAG, "views:" + containerView);
    }

    private View getQuickInputView() {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_todo_list_row_first, scrollView, false);
        final EditText editText = (EditText) view.findViewById(R.id.editText);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        createNewItem(editText.getText().toString());
                        editText.setText("");
                    }
                    return true;
                }
                return false;
            }
        });

        ImageView iv = (ImageView) view.findViewById(R.id.drag_handle);
        iv.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                    final View srcView = (View) event.getLocalState();
                    srcView.post(new Runnable() {
                        @Override
                        public void run() {
                            srcView.setVisibility(View.VISIBLE);
                        }
                    });
                }
                return true;
            }
        });
        return view;
    }

    private void createNewItem(String s) {
        TodoNoteMapper.TodoItem item = new TodoNoteMapper.TodoItem(false, s);
        todoItems.add(0, item);
        View view = getViewForRow(item);
        containerView.addView(view, 1);
    }

    @Override
    public void setNote(Note note) {
        this.note = note;
        this.todoItems = this.note.getTodoItems();
        updateViews();
    }

    @Override
    public Note getNote() {
        note.setTodoItems(todoItems);
        return note;
    }

    @Override
    public Note.NoteType getNoteType() {
        return Note.NoteType.TodoNote;
    }

    private View getViewForRow(final TodoNoteMapper.TodoItem item) {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_todo_list_row, scrollView, false);
        view.setTag(item);
        view.setBackgroundResource(DRAG_NORMAL_COLOR);

        final EditText tv = (EditText) view.findViewById(R.id.editText);
        tv.setText(item.content);
        tv.addTextChangedListener(new TodoTextWatcher(tv, item));
        tv.setOnKeyListener(new View.OnKeyListener() {
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

        final ImageButton checked = (ImageButton) view.findViewById(R.id.checkbox_checked);
        final ImageButton unchecked = (ImageButton) view.findViewById(R.id.checkbox_unchecked);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTodoItemCompletedState(item);
                updateTodoItemViews(checked, item, unchecked, view);
            }
        };
        checked.setOnClickListener(clickListener);
        unchecked.setOnClickListener(clickListener);
        updateCheckboxVisibility(checked, item, unchecked);

        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                deleteTodoItem(view);
                return true;
            }
        };
        checked.setOnLongClickListener(longClickListener);
        unchecked.setOnLongClickListener(longClickListener);

        ImageView iv = (ImageView) view.findViewById(R.id.drag_handle);
        enableDragReorder(view, iv);

        ImageButton deleteItemButton = (ImageButton) view.findViewById(R.id.delete_todo_item);
        deleteItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTodoItem(view);
            }
        });
        return view;
    }

    private void updateTodoItemViews(ImageButton checked, TodoNoteMapper.TodoItem item, ImageButton unchecked, View rowView) {
        updateCheckboxVisibility(checked, item, unchecked);
        containerView.removeView(rowView);
        int index = item.completed ? containerView.getChildCount() : 1;
        containerView.addView(rowView, index);
    }

    private void deleteTodoItem(View rowView) {
        TodoNoteMapper.TodoItem item = (TodoNoteMapper.TodoItem) rowView.getTag();
        todoItems.remove(item);
        removeViewForItem(item);
    }

    private void enableDragReorder(final View rowView, ImageView iv) {
        iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(rowView) {
                        @Override
                        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
                            shadowSize.set(rowView.getWidth(), rowView.getHeight());
                            shadowTouchPoint.set(0, rowView.getHeight() / 2);
                        }
                    };
                    rowView.startDrag(data, shadowBuilder, rowView, 0);
                    rowView.setVisibility(View.INVISIBLE);
                    return true;
                } else {
                    return false;
                }
            }
        });

        iv.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final View srcView = (View) event.getLocalState();
                final View dstView = (View) v.getParent();
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        dstView.setBackgroundResource(DRAG_HIGHLIGHT_COLOR);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        dstView.setBackgroundResource(DRAG_NORMAL_COLOR);
                        break;
                    case DragEvent.ACTION_DROP:
                        reorderTodoItem(srcView, dstView);
                        dstView.setBackgroundResource(DRAG_NORMAL_COLOR);
                        break;
                }
                return true;
            }
        });
    }

    private void reorderTodoItem(View srcView, View dstView) {
        TodoNoteMapper.TodoItem srcItem = (TodoNoteMapper.TodoItem) srcView.getTag();
        int srcIndex = todoItems.indexOf(srcItem);

        TodoNoteMapper.TodoItem dstItem = (TodoNoteMapper.TodoItem) dstView.getTag();
        int dstIndex = todoItems.indexOf(dstItem);
//        if (srcIndex < dstIndex)    --dstIndex;

        todoItems.remove(srcIndex);
        todoItems.add(dstIndex, srcItem);

        containerView.removeView(srcView);
        containerView.addView(srcView, dstIndex+1);
    }

    private void toggleTodoItemCompletedState(TodoNoteMapper.TodoItem item) {
        item.completed = !item.completed;
        todoItems.remove(item);
        int index = item.completed ? todoItems.size() : 0;
        todoItems.add(index, item);
    }

    private void updateCheckboxVisibility(ImageButton checked, TodoNoteMapper.TodoItem item, ImageButton unchecked) {
        checked.setVisibility(item.completed ? View.VISIBLE : View.GONE);
        unchecked.setVisibility(item.completed ? View.GONE : View.VISIBLE);
    }

    private void onEditTextEnterPressed() {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.note_detail_todo, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear_completed) {
            clearCompletedTodoItems();
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearCompletedTodoItems() {
        List<TodoNoteMapper.TodoItem> completedItems = new ArrayList<TodoNoteMapper.TodoItem>();
        for (TodoNoteMapper.TodoItem item : todoItems) {
            if (item.completed)     completedItems.add(item);
        }
        for (TodoNoteMapper.TodoItem item : completedItems) {
            todoItems.remove(item);
            removeViewForItem(item);
        }
    }

    private void removeViewForItem(TodoNoteMapper.TodoItem item) {
        for (int i=0; i<containerView.getChildCount(); ++i) {
            View view = containerView.getChildAt(i);
            if (view.getTag() == item) containerView.removeView(view);
        }
    }

    private class TodoTextWatcher implements TextWatcher {
        private final EditText editText;
        private TodoNoteMapper.TodoItem item;

        private TodoTextWatcher(EditText editText, TodoNoteMapper.TodoItem item) {
            this.editText = editText;
            this.item = item;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            item.content = s.toString();
            Log.d(TAG, "text changed: " + note);
        }

        @Override
        public void afterTextChanged(Editable s) { }
    }
}
