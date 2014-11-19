package simpzan.notes.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoqing.zgg on 2014/11/10.
 */
public class TodoNoteMapper {
    public static final String TODO_MARKDOWN_UNCHECKED = "- [ ] ";
    public static final String TODO_MARKDOWN_CHECKED = "- [x] ";
    public static final int TODO_MARKDOWN_CHECK_CHAR_INDEX = 3;

    public List<TodoItem> deserialze(String todoString) {
        List<TodoItem> items = new ArrayList<TodoItem>();
        String[] lines = todoString.split("\n");
        for (String line : lines) {
            TodoItem item = createTodoItem(line);
            items.add(item);
        }
        return items;
    }

    private TodoItem createTodoItem(String line) {
        TodoItem item = new TodoItem();
        if (line.startsWith(TODO_MARKDOWN_UNCHECKED) || line.startsWith(TODO_MARKDOWN_CHECKED)) {
            item.content = line.substring(TODO_MARKDOWN_CHECKED.length());
            item.completed = line.charAt(TODO_MARKDOWN_CHECK_CHAR_INDEX) == 'x';
        } else {
            item.content = line;
        }
        return item;
    }

    public String serialize(List<TodoItem> todoItems) {
        StringBuilder builder = new StringBuilder();
        for (TodoItem item : todoItems) {
            String prefix = item.completed ? TODO_MARKDOWN_CHECKED : TODO_MARKDOWN_UNCHECKED;
            builder.append(prefix).append(item.content).append('\n');
        }
        return builder.toString();
    }

    public static class TodoItem {
        public boolean completed;
        public String content;

        public TodoItem() { }

        public TodoItem(boolean completed, String content) {
            this.completed = completed;
            this.content = content;
        }

        @Override
        public String toString() {
            return "TodoItem{" +
                    "completed=" + completed +
                    ", content='" + content + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TodoItem item = (TodoItem) o;
            if (completed != item.completed) return false;
            if (content != null ? !content.equals(item.content) : item.content != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (completed ? 1 : 0);
            result = 31 * result + (content != null ? content.hashCode() : 0);
            return result;
        }
    }

}
