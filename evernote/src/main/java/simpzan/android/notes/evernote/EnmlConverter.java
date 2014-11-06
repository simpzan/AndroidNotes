package simpzan.android.notes.evernote;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by guoqing.zgg on 2014/11/5.
 */
public class EnmlConverter {
    private static final String DOC_TYPE = " en-note SYSTEM 'http://xml.evernote.com/pub/enml2.dtd'";
    private static final String EN_TODO_TAG = "en-todo";
    private final String EN_NOTE_TAG = "en-note";
    private final String TODO_MARKDOWN_UNCHECKED = "- [ ] ";
    private final String TODO_MARKDOWN_CHECKED = "- [x] ";
    private final String EN_TODO_ATTRIBUTE_CHECKED = "checked";
    private String[] TAGS = {
            "div",
            "h1",
            "h2",
            "p",
//            "br",
    };
    private Set<String> tags = new HashSet<String>();

    public EnmlConverter() {
        tags.addAll(Arrays.asList(TAGS));
    }

    public String enmlToPlainText(String enml) {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new StringReader(enml));
            skipToEnNoteTag(parser);
            return parseEnNoteTag(parser);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String parseEnNoteTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, EN_NOTE_TAG);
        StringBuilder builder = new StringBuilder();
        for (int event = parser.getEventType(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
            if (event == XmlPullParser.TEXT) {
                builder.append(parser.getText());
            } else if (event == XmlPullParser.END_TAG && tags.contains(parser.getName())) {
                builder.append("\n");
            } else if (event == XmlPullParser.START_TAG && EN_TODO_TAG.equals(parser.getName())) {
                String checkedValue = parser.getAttributeValue(null, EN_TODO_ATTRIBUTE_CHECKED);
                String checked = "true".equals(checkedValue) ? "x" : " ";
                builder.append("- [" + checked + "] ");
            }
        }
        return builder.toString();
    }

    private void skipToEnNoteTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        for (int event = parser.getEventType(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
            if (event == XmlPullParser.START_TAG && "en-note".equals(parser.getName())) break;
        }
    }

    public String plainTextToEnml(String text) {
        try {
            StringWriter writer = new StringWriter();
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", null);
            serializer.docdecl(DOC_TYPE);
            serializer.startTag("", EN_NOTE_TAG);

            generateEnmlBody(text, serializer);
            serializer.endTag("", EN_NOTE_TAG);
            serializer.endDocument();
            return writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void generateEnmlBody(String text, XmlSerializer serializer) throws IOException {
        String[] lines = text.split("\n");
        for (String line : lines) {
            serializer.startTag("", "div");
            generateDivForALine(serializer, line);
            serializer.endTag("", "div");
        }
    }

    private void generateDivForALine(XmlSerializer serializer, String line) throws IOException {
        if (line.length() == 0) {
            serializer.startTag("", "br");
            serializer.attribute(null, "clear", "none");
            serializer.endTag("", "br");
            return;
        }

        if (line.startsWith(TODO_MARKDOWN_UNCHECKED) || line.startsWith(TODO_MARKDOWN_CHECKED)) {
            serializer.startTag(null, EN_TODO_TAG);
            String checkedValue = line.charAt(3) == 'x' ? "true" : "false";
            serializer.attribute(null, EN_TODO_ATTRIBUTE_CHECKED, checkedValue);
            serializer.endTag(null, EN_TODO_TAG);
            line = line.substring(TODO_MARKDOWN_CHECKED.length());
        }

        serializer.text(line);
    }

    private void print(String key, String text) {
        System.out.println(key + ":" + text + "|end");
    }
}
