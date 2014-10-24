package simpzan.android.notes.ui;

import android.app.Activity;
import android.os.Bundle;

import simpzan.android.notes.NotesApp;

/**
 * Created by guoqing.zgg on 2014/10/24.
 */
public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotesApp app = (NotesApp) getApplication();
        app.inject(this);
    }
}
