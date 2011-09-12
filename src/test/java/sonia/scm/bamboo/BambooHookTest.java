package sonia.scm.bamboo;

import com.google.inject.Provider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import sonia.scm.net.HttpClient;

/**
 * Test class for the BambooHook.
 *
 * Should cover some more test cases ;-)
 *
 * @author Stephan Oudmaijer
 */
public class BambooHookTest {

    private BambooHook bambooHook;
    @Mock
    private Provider<HttpClient> httpClientProvider;


    @Before
    public void setup() {
        bambooHook = new BambooHook(httpClientProvider);
    }

    @Test
    public void testCreateUrl() {
        String url = bambooHook.createUrl("http://bamboo.url", "MY-KEY");
        Assert.assertEquals(url, "http://bamboo.url/api/rest/updateAndBuild.action?buildKey=MY-KEY");
    }
}
