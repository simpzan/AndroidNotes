package simpzan.android.notes;

import android.content.Context;

import com.evernote.client.android.EvernoteSession;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import simpzan.android.notes.db.SqliteNoteRepository;
import simpzan.android.notes.evernote.EvernoteNoteRepository;
import simpzan.notes.domain.NoteManager;
import simpzan.android.notes.ui.NoteDetailActivity;
import simpzan.android.notes.ui.NoteListActivity;
import simpzan.notes.domain.SyncManager;

/**
 * Created by guoqing.zgg on 2014/10/24.
 * dagger module for providing constructors.
 */
@Module(
    injects = {
        NoteListActivity.class, NoteDetailActivity.class
    }
)
public class AppModule {

    private Context context;

    public AppModule(Context context) { this.context = context; }

    @Provides
    @Singleton
    Context provideContext() {
        return context;
    }

    @Provides
    @Singleton
    NoteManager provideNoteManager(SqliteNoteRepository repo) {
        return new NoteManager(repo);
    }

    @Provides
    @Singleton
    AsyncNoteManager provideAsyncNoteManager(NoteManager manager) {
        return new AsyncNoteManager(manager);
    }

    @Provides
    @Singleton
    EvernoteNoteRepository provideEvernoteNoteRepository(Context context, EvernoteSession session) {
        EvernoteNoteRepository repo = new EvernoteNoteRepository(context, session);
        return repo;
    }

    @Provides
    @Singleton
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
        return EvernoteSession.getInstance(context,
                EvernoteNoteRepository.CONSUMER_KEY,
                EvernoteNoteRepository.CONSUMER_SECRET,
                EvernoteSession.EvernoteService.SANDBOX,
                false);
    }
}
