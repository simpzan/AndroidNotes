package simpzan.android.notes.db;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

/**
 * Created by guoqing.zgg on 2014/10/22.
 */
@RealmClass
public class NoteRealmObject extends RealmObject {
    private String title;
    private String content;
    private long modified;
    private long id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
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
