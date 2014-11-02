package simpzan.common;

/**
 * Created by guoqing.zgg on 2014/11/2.
 */
public class StringUtil {
    public static boolean isEmpty(String str) {
        if (str == null)  return true;

        return str.length() == 0;
    }

    public static boolean isEmptyTrimmed(String str) {
        boolean empty = isEmpty(str);
        if (empty)  return empty;

        return str.trim().length() == 0;
    }
}
