package simpzan.notes.domain;

import java.util.Date;
import java.util.List;

import simpzan.common.StringUtil;

public class Note {
    public enum NoteType {
        PlainNote,
        TodoNote,
        MarkdownNote,
    }
    private String title;
    private String content;

    public NoteType getContentType() {
        boolean isTodoNote = getContent().contains(TodoNoteMapper.TODO_MARKDOWN_CHECKED)
                || getContent().contains(TodoNoteMapper.TODO_MARKDOWN_UNCHECKED);
        return isTodoNote ? NoteType.TodoNote : NoteType.PlainNote;
    }

    private Date modified;
    private long id;

    private String guid;
    private long updateSequenceNumber;
    private boolean deleted;
    private boolean dirty = true;

    private static TodoNoteMapper todoNoteMapper = new TodoNoteMapper();

    public Note(String title) {
        this.title = title;
        this.modified = new Date();
        this.content = "";
    }

    public Note(Note note) {
        this.title = note.title;
        this.content = note.content;
        this.modified = note.modified;
        this.id = note.id;

        this.guid = note.guid;
        this.updateSequenceNumber = note.updateSequenceNumber;
        this.deleted = note.deleted;
        this.dirty = note.dirty;
    }

    public String getPlainTextContent() {
        if (getContentType() == NoteType.PlainNote)  return getContent();

        // contentType == NoteType.TodoNote
        StringBuilder builder = new StringBuilder();
        List<TodoNoteMapper.TodoItem> items = getTodoItems();
        for (TodoNoteMapper.TodoItem item : items) {
            builder.append(item.content).append('\n');
        }
        return builder.toString();
    }

    public List<TodoNoteMapper.TodoItem> getTodoItems() {
        return todoNoteMapper.deserialze(getContent());
    }

    public void setTodoItems(List<TodoNoteMapper.TodoItem> items) {
        setContent(todoNoteMapper.serialize(items));
    }

    public boolean isUpdated() {
        return dirty && !deleted;
    }

    public boolean isNew() {
        return StringUtil.isEmptyTrimmed(guid) || updateSequenceNumber == 0;
    }

    public void setNew() {
        guid = null;
        updateSequenceNumber = 0;
        id = 0;
        deleted = false;
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public long getUpdateSequenceNumber() {
        return updateSequenceNumber;
    }

    public void setUpdateSequenceNumber(long updateSequenceNumber) {
        this.updateSequenceNumber = updateSequenceNumber;
    }

    @Override
    public String toString() {
        return "Note{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", modified=" + modified +
                ", id=" + id +
                ", guid='" + guid + '\'' +
                ", updateSequenceNumber=" + updateSequenceNumber +
                ", deleted=" + deleted +
                ", dirty=" + dirty +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        if (deleted != note.deleted) return false;
        if (dirty != note.dirty) return false;
        if (id != note.id) return false;
        if (updateSequenceNumber != note.updateSequenceNumber) return false;
        if (content != null ? !content.equals(note.content) : note.content != null) return false;
        if (guid != null ? !guid.equals(note.guid) : note.guid != null) return false;
        if (!modified.equals(note.modified)) return false;
        if (!title.equals(note.title)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + modified.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (int) (updateSequenceNumber ^ (updateSequenceNumber >>> 32));
        result = 31 * result + (deleted ? 1 : 0);
        result = 31 * result + (dirty ? 1 : 0);
        return result;
    }

}
