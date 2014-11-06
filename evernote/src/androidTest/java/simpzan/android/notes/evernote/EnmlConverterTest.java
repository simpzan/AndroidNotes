package simpzan.android.notes.evernote;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

@RunWith(RobolectricTestRunner.class)
public class EnmlConverterTest {
    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testEnmlToPlainText() throws Exception {
        EnmlConverter converter = new EnmlConverter();
        String enml = getFileContent("evernote_note.enml");
        String text = getFileContent("evernote_note.txt");
        String result = converter.enmlToPlainText(enml);
        Assert.assertEquals(text, result);
    }

    @Test
    public void testPlainTextToEnml() throws Exception {
        EnmlConverter converter = new EnmlConverter();
        String text = getFileContent("evernote_note.txt");
        String enml = converter.plainTextToEnml(text);
        String resultText = converter.enmlToPlainText(enml);
        Assert.assertEquals(text, resultText);
    }

    private void test() {
        String text = "first\n\nthird\n\n";
        String[] parts = text.split("\n", -1);
        print("parts", Arrays.toString(parts));
    }

    private void print(String key, String text) {
        System.out.println(key + ":" + text + "|end");
    }

    private String getFileContent(String filename) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        Assert.assertNotNull(inputStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String str;
        while ((str = reader.readLine()) != null) {
            builder.append(str + "\n" );
        }
        inputStream.close();
        return builder.toString();
    }
}