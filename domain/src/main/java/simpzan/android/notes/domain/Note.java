package simpzan.android.notes.domain;

import java.util.Date;

public class Note {
    private String title;
    private String content;
    private Date modified;
    private long id;

    public Note(String title) {
        this.title = title;
        this.modified = new Date();
        this.content = "";
        this.id = 0;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        if (id != note.id) return false;
        if (content != null ? !content.equals(note.content) : note.content != null) return false;
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
        return result;
    }

    @Override
    public String toString() {
        return "Note{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", modified=" + modified.getTime() +
                ", id=" + id +
                '}';
    }
}
