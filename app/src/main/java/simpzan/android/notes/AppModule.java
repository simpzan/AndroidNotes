package simpzan.android.notes;

import android.content.Context;

import com.evernote.client.android.EvernoteSession;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import simpzan.android.notes.db.RealmNoteRepository;
import simpzan.android.notes.db.SqliteNoteRepository;
import simpzan.android.notes.evernote.EvernoteNoteRepository;
import simpzan.notes.domain.INoteRepository;
import simpzan.notes.domain.NoteManager;
import simpzan.android.notes.ui.NoteDetailActivity;
import simpzan.android.notes.ui.NoteListActivity;
import simpzan.notes.domain.SyncManager;

/**
 * Created by guoqing.zgg on 2014/10/24.
 */
@Module(
    injects = {
        NoteListActivity.class, NoteDetailActivity.class
    }
)
public class AppModule {
    private static final String CONSUMER_KEY = "simpzan-9925";
    private static final String CONSUMER_SECRET = "60e4fa505ecb18b2";

    private Context context;

    public AppModule(Context context) { this.context = context; }

    @Provides @Singleton
    Context provideContext() {
        return context;
    }

    @Provides
    @Singleton
    NoteManager provideNoteManager(INoteRepository repo) {
        return new NoteManager(repo);
    }

    @Provides
    @Singleton
    AsyncNoteManager provideAsyncNoteManager(NoteManager manager) {
        return new AsyncNoteManager(manager);
    }

    @Provides
    INoteRepository provideNoteRepository(Context context) {
        INoteRepository repo = new SqliteNoteRepository(context);
        return repo;
    }

    @Provides
    EvernoteNoteRepository provideEvernoteNoteRepository(EvernoteSession session) {
        EvernoteNoteRepository repo = new EvernoteNoteRepository(session);
        return repo;
    }

    @Provides
    SqliteNoteRepository provideSqliteNoteRepository(Context context) {
        SqliteNoteRepository repo = new SqliteNoteRepository(context);
        return repo;
    }

    @Provides
    @Singleton
    SyncManager providerSyncManager(SqliteNoteRepository localRepo, EvernoteNoteRepository remoteRepo) {
        return new SyncManager(localRepo, remoteRepo);
    }

    @Provides
    @Singleton
    EvernoteSession providerEvernoteSession(Context context) {
        return EvernoteSession.getInstance(context, CONSUMER_KEY, CONSUMER_SECRET, EvernoteSession.EvernoteService.SANDBOX, false);
    }
}
