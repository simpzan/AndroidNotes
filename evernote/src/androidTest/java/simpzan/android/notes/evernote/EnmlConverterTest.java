package simpzan.android.notes.evernote;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

@RunWith(RobolectricTestRunner.class)
public class EnmlConverterTest {
    private EnmlConverter converter;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        converter = new EnmlConverter();
    }

    @Test
    public void testEnmlToPlainText() throws Exception {
        String enml = getFileContent("evernote_note.enml");
        String text = getFileContent("evernote_note.txt");
        String result = converter.enmlToPlainText(enml);
        Assert.assertEquals(text, result);
    }

    @Test
    public void testPlainTextToEnml() throws Exception {
        String text = getFileContent("evernote_note.txt");
        String enml = converter.plainTextToEnml(text);
        print("enml", enml);
        String resultText = converter.enmlToPlainText(enml);
        Assert.assertEquals(text, resultText);
    }

    @Test
    public void testTodoEnmlToPlainText() throws IOException {
        String enml = getFileContent("evernote_todo.enml");
        String result_text = converter.enmlToPlainText(enml);
        String text = getFileContent("evernote_todo.txt");
        Assert.assertEquals(text, result_text);
    }

    @Test
    public void testTodoPlainTextToEnml() throws IOException {
        String text = getFileContent("evernote_todo.txt");
        String enml = converter.plainTextToEnml(text);
        print("enml", enml);
        Assert.assertTrue(enml.contains("<en-todo"));
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