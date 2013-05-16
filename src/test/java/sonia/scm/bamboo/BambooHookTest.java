package sonia.scm.bamboo;

import com.google.inject.Provider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.net.HttpClient;
import sonia.scm.net.HttpRequest;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for the BambooHook.
 * <p/>
 * Should cover some more test cases ;-)
 *
 * @author Stephan Oudmaijer
 */
@RunWith(MockitoJUnitRunner.class)
public class BambooHookTest {

    private BambooHook bambooHook;

    @Mock
    private Provider<HttpClient> httpClientProvider;

    @Mock
    private BambooPluginConfigRepository repository;

    @Before
    public void setup() {
        bambooHook = new BambooHook(httpClientProvider, repository);
    }

    @Test
    public void testCreateUrl() {
        String url = bambooHook.createUrl("http://bamboo.url", "MY-KEY");
        assertEquals(url, "http://bamboo.url/api/rest/updateAndBuild.action?buildKey=MY-KEY");
    }

    @Test
    public void testTriggerBamboo() throws IOException {
        RepositoryHookEvent rhe = Mockito.mock(RepositoryHookEvent.class);
        Repository repo = Mockito.mock(Repository.class);

        when(rhe.getRepository()).thenReturn(repo);
        when(repo.getProperty(BambooHook.PROPERTY_BAMBOO_URL)).thenReturn(null);
        when(repo.getProperty(BambooHook.PROPERTY_BAMBOO_PLANS)).thenReturn("A,B");

        BambooPluginConfig config = new BambooPluginConfig("http://bamboo", null, null, false);
        when(repository.getConfig()).thenReturn(config);

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        when(httpClientProvider.get()).thenReturn(httpClient);

        bambooHook.onEvent(rhe);

        ArgumentCaptor<HttpRequest> httpRequest = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(2)).post(httpRequest.capture());

        assertTrue(httpRequest.getValue().getUrl().startsWith("http://bamboo/api/rest/updateAndBuild.action?buildKey="));
    }

    @Test
    public void testTriggerBambooWithAuthentication() throws IOException {
        RepositoryHookEvent rhe = Mockito.mock(RepositoryHookEvent.class);
        Repository repo = Mockito.mock(Repository.class);

        when(rhe.getRepository()).thenReturn(repo);
        when(repo.getProperty(BambooHook.PROPERTY_BAMBOO_URL)).thenReturn(null);
        when(repo.getProperty(BambooHook.PROPERTY_BAMBOO_PLANS)).thenReturn("A,B");

        BambooPluginConfig config = new BambooPluginConfig("http://bamboo", "user", "pass", false);
        when(repository.getConfig()).thenReturn(config);

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        when(httpClientProvider.get()).thenReturn(httpClient);

        bambooHook.onEvent(rhe);

        ArgumentCaptor<HttpRequest> httpRequest = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(2)).post(httpRequest.capture());

        assertTrue(httpRequest.getValue().getUrl().startsWith("http://bamboo/api/rest/updateAndBuild.action?buildKey="));
        assertEquals("pass", httpRequest.getValue().getPassword());
        assertEquals("user", httpRequest.getValue().getUsername());
    }

    @Test
    public void testTriggerBambooWithOverrideUrl() throws IOException {
        RepositoryHookEvent rhe = Mockito.mock(RepositoryHookEvent.class);
        Repository repo = Mockito.mock(Repository.class);

        when(rhe.getRepository()).thenReturn(repo);
        when(repo.getProperty(BambooHook.PROPERTY_BAMBOO_URL)).thenReturn("http://override");
        when(repo.getProperty(BambooHook.PROPERTY_BAMBOO_PLANS)).thenReturn("A,B");

        BambooPluginConfig config = new BambooPluginConfig("http://bamboo", "user", "pass", true);
        when(repository.getConfig()).thenReturn(config);

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        when(httpClientProvider.get()).thenReturn(httpClient);

        bambooHook.onEvent(rhe);

        ArgumentCaptor<HttpRequest> httpRequest = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(2)).post(httpRequest.capture());

        System.err.println(httpRequest.getValue().getUrl());

        assertTrue(httpRequest.getValue().getUrl().startsWith("http://override/api/rest/updateAndBuild.action?buildKey="));
        assertEquals("pass", httpRequest.getValue().getPassword());
        assertEquals("user", httpRequest.getValue().getUsername());
    }
}