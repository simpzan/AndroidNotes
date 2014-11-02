package simpzan.notes.domain;

import java.util.Date;

public class Note {
    private String title;
    private String content;
    private Date modified;
    private long id;

    private String guid;
    private long updateSequenceNumber;
    private boolean deleted;
    private boolean updated;

    public Note(String title) {
        this.title = title;
        this.modified = new Date();
        this.content = "";
        this.id = 0;
    }

    public boolean isChanged() {
        return isNew() || isUpdated() || isDeleted();
    }

    public boolean isNew() {
        return guid == null || guid.trim().length() == 0 || updateSequenceNumber == 0;
    }

    public void setNew() {
        guid = null;
        updateSequenceNumber = 0;
        id = 0;
        deleted = false;
        updated = true;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        if (deleted != note.deleted) return false;
        if (updated != note.updated) return false;
        if (guid != note.guid) return false;
        if (id != note.id) return false;
        if (updateSequenceNumber != note.updateSequenceNumber) return false;
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
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (int) (updateSequenceNumber ^ (updateSequenceNumber >>> 32));
        result = 31 * result + (deleted ? 1 : 0);
        result = 31 * result + (updated ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Note{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", modified=" + modified +
                ", id=" + id +
                ", guid=" + guid +
                ", updateSequenceNumber=" + updateSequenceNumber +
                ", deleted=" + deleted +
                ", updated=" + updated +
                '}';
    }

}
