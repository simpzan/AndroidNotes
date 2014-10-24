package simpzan.android.notes;

import android.app.Application;

import dagger.ObjectGraph;

/**
 * Created by guoqing.zgg on 2014/10/24.
 */
public class NotesApp extends Application {
    private ObjectGraph graph;

    @Override
    public void onCreate() {
        super.onCreate();
        graph = ObjectGraph.create(new AppModule(this));
    }

    public void inject(Object object) {  graph.inject(object);  }
}
