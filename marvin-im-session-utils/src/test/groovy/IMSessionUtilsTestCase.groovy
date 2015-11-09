import com.beolnix.marvin.im.IMSessionUtils
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

/**
 * Created by DAtmakin on 11/3/2015.
 */
class IMSessionUtilsTestCase {
    def imSessionUtils = new IMSessionUtils();

    @Test
    public void isCommandPositiveTest() {
        assertTrue(imSessionUtils.isCommand("!test", "!"))
    }

    @Test
    public void isCommandNegativeTest1() {
        assertFalse(imSessionUtils.isCommand("!test", "/"))
    }

    @Test
    public void isCommandNegativeTest2() {
        assertFalse(imSessionUtils.isCommand("test", "/"))
    }

    @Test
    public void parseCommandPositiveTest() {
        def result = imSessionUtils.parseCommand("!start franchesco", "!")
        assertTrue(result.isPresent())
        assertEquals("start", result.get())
    }

    @Test
    public void parseCommandNegativeTest1() {
        def result = imSessionUtils.parseCommand("!start franchesco", "/")
        assertFalse(result.isPresent())
    }

    @Test
    public void parseCommandNegativeTest2() {
        def result = imSessionUtils.parseCommand("start franchesco", "!")
        assertFalse(result.isPresent())
    }

    @Test
    public void parseCommandAttributesPositiveTest1() {
        def result = imSessionUtils.parseCommandAttributes("!start franchesco one", "start", "!")
        assertTrue(result.isPresent())
        assertEquals("franchesco one", result.get())
    }

    @Test
    public void parseCommandAttributesPositiveTest2() {
        def result = imSessionUtils.parseCommandAttributes("!start franchesco one", "!")
        assertTrue(result.isPresent())
        assertEquals("franchesco one", result.get())
    }
}
