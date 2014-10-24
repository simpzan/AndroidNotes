package simpzan.android.notes;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import simpzan.android.notes.db.NoteRepository;
import simpzan.android.notes.domain.INoteRepository;
import simpzan.android.notes.domain.NoteManager;
import simpzan.android.notes.ui.NoteDetailActivity;
import simpzan.android.notes.ui.NoteListActivity;

/**
 * Created by guoqing.zgg on 2014/10/24.
 */
@Module(
    injects = {
        NoteListActivity.class, NoteDetailActivity.class
    }
)
public class AppModule {

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
    INoteRepository provideNoteRepository(Context context) {
        INoteRepository repo = new NoteRepository(context);
        return repo;
    }

}
