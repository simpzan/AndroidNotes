package simpzan.android.notes;

import java.util.Date;

public class Note {
    private String title;
    private String content;
    private Date modified;
    private long id;

    public Note(String title) {
        this.title = title;
        this.modified = new Date();
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
}
