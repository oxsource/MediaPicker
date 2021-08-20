package pizzk.media.picker;

import org.junit.Test;

import static org.junit.Assert.*;

import pizzk.media.picker.utils.TimeUtils;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void duration_isCorrect() {
        TimeUtils.INSTANCE.duration(-1);
    }
}