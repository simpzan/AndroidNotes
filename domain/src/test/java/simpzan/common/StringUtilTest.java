package simpzan.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by guoqing.zgg on 2014/11/2.
 */
public class StringUtilTest {
    @Test
    public void testIsEmpty() {
        String str = null;
        Assert.assertTrue(StringUtil.isEmpty(str));

        str = "";
        Assert.assertTrue(StringUtil.isEmpty(str));

        str = " ";
        Assert.assertFalse(StringUtil.isEmpty(str));
    }

    @Test
    public void testIsEmptyTrimmed() {
        String str = null;
        Assert.assertTrue(StringUtil.isEmptyTrimmed(str));

        str = "";
        Assert.assertTrue(StringUtil.isEmptyTrimmed(str));

        str = " ";
        Assert.assertTrue(StringUtil.isEmptyTrimmed(str));

        str = "   ";
        Assert.assertTrue(StringUtil.isEmptyTrimmed(str));

        str = "notEmpty";
        Assert.assertFalse(StringUtil.isEmptyTrimmed(str));
    }
}
