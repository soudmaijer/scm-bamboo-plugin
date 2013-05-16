/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.bamboo;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.HttpClient;
import sonia.scm.net.HttpRequest;
import sonia.scm.net.HttpResponse;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHook;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.util.Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Stephan Oudmaijer
 */
@Extension
public class BambooHook implements RepositoryHook {

    /**
     * Field description
     */
    public static final String PROPERTY_BAMBOO_PLANS = "bamboo.plans";

    /**
     * Field description
     */
    public static final String PROPERTY_BAMBOO_URL = "bamboo.url";

    /**
     * the logger for BambooHook
     */
    private static final Logger logger = LoggerFactory.getLogger(BambooHook.class);

    /**
     * Config repository
     */
    private final BambooPluginConfigRepository bambooPluginConfigRepository;

    //~--- constructors ---------------------------------------------------------

    /**
     * Constructs the hook class.
     *
     * @param httpClientProvider the httpClient
     */
    @Inject
    public BambooHook(final Provider<HttpClient> httpClientProvider, final BambooPluginConfigRepository bambooPluginConfigRepository) {
        this.httpClientProvider = httpClientProvider;
        this.bambooPluginConfigRepository = bambooPluginConfigRepository;
    }

    //~--- methods --------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEvent(RepositoryHookEvent event) {
        Repository repository = event.getRepository();

        if (repository != null) {
            handleRepositoryEvent(repository);
        } else if (logger.isWarnEnabled()) {
            logger.warn("receive repository hook without repository");
        }
    }

    //~--- get methods ----------------------------------------------------------

    /**
     * POST_RECEIVE
     *
     * @return POST_RECEIVE repository hook type.
     */
    @Override
    public Collection<RepositoryHookType> getTypes() {
        return Arrays.asList(RepositoryHookType.POST_RECEIVE);
    }

    /**
     * {@inheritDoc}
     *
     * @return true
     */
    @Override
    public boolean isAsync() {
        return true;
    }

    //~--- methods --------------------------------------------------------------

    /**
     * Creates the Bamboo update and build trigger URL
     *
     * @param url      base url
     * @param buildKey the plan key
     * @return an URL in String format.
     */
    String createUrl(String url, String buildKey) {
        if (!url.endsWith("/")) {
            url = url.concat("/");
        }
        return url.concat("api/rest/updateAndBuild.action?buildKey=").concat(buildKey);
    }

    /**
     * Method description
     *
     * @param repository the repository on which the trigger fired.
     */
    private void handleRepositoryEvent(Repository repository) {
        String url = repository.getProperty(PROPERTY_BAMBOO_URL);
        String plans = repository.getProperty(PROPERTY_BAMBOO_PLANS);
        String username = null;
        String password = null;
        BambooPluginConfig config = bambooPluginConfigRepository.getConfig();

        if (config != null) {
            url = Util.isNotEmpty(url) && config.isAllowOverride() ? url : config.getUrl();
            password = config.getPassword();
            username = config.getUsername();
        }

        if (Util.isNotEmpty(url) && Util.isNotEmpty(plans)) {

            String[] planKeys = new String[]{plans};

            if (plans.contains(",")) {
                planKeys = plans.split(",");
            }

            for (String key : planKeys) {
                String planUrl = createUrl(url, key.trim());
                if (logger.isInfoEnabled()) {
                    logger.info("call bamboo at {}", planUrl);
                }

                try {
                    sendRequest(planUrl, username, password);
                } catch (IOException ex) {
                    logger.error("Could not send request to bamboo", ex);
                }
            }
        }
    }

    /**
     * Sends the Request, logs an error on all return codes > 400.
     *
     * @param url Url to post data to.
     * @throws IOException exception
     */
    private void sendRequest(String url, String username, String password) throws IOException {
        HttpClient httpClient = httpClientProvider.get();
        HttpRequest request = new HttpRequest(url);
        if (Util.isNotEmpty(username) && Util.isNotEmpty(password)) {
            request.setBasicAuthentication(username, password);
        }

        HttpResponse response = httpClient.post(request);
        if (response != null) {
            int sc = response.getStatusCode();

            if (sc >= 400) {
                logger.error("bamboo returned status code {}", sc);
            }
        }
    }

    //~--- fields ---------------------------------------------------------------

    /**
     * Field description
     */
    private Provider<HttpClient> httpClientProvider;
}
