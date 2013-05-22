package sonia.scm.bamboo;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class BambooPluginConfigResourceTest {

    private BambooPluginConfigResource bambooPluginConfigResource;

    @Mocked
    private BambooPluginConfigRepository repository;
    @Mocked
    private SecurityUtils securityUtils;

    @Before
    public void setup() {
        bambooPluginConfigResource = new BambooPluginConfigResource(repository);

        final MockUp<Subject> subjectMockUp = new MockUp<Subject>() {
            @Mock
            public void checkRole(java.lang.String role) throws org.apache.shiro.authz.AuthorizationException {
            }
        };

        new NonStrictExpectations() {
            {
                SecurityUtils.getSubject();
                returns(subjectMockUp.getMockInstance());

            }
        };
    }

    @Test
    public void testGetConfig() {

        new NonStrictExpectations() {
            {
                repository.getConfig();
                returns(new BambooPluginConfig());
            }
        };

        final BambooPluginConfig config = bambooPluginConfigResource.getConfig();

        assertEquals(null, config.getPassword());
        assertEquals(null, config.getUsername());
        assertEquals(null, config.getUrl());
        assertEquals(false, config.isAllowOverride());
    }

    @Test
    public void testStoreConfig(final UriInfo uriInfo, final Response response, final Response.ResponseBuilder responseBuilder) throws IOException, URISyntaxException {

        final BambooPluginConfig config = new BambooPluginConfig();

        new NonStrictExpectations() {
            {
                URI uri = new URI("/ci/bamboo/store");
                repository.storeConfig(config);
                uriInfo.getRequestUri();
                returns(uri);
                Response.created(uri);
                returns(responseBuilder);
            }
        };

        bambooPluginConfigResource.setConfig(uriInfo, config);
    }
}
