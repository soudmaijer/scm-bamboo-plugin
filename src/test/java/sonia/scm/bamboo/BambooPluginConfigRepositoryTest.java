package sonia.scm.bamboo;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;

import static junit.framework.Assert.assertNotNull;

@RunWith(JMockit.class)
public class BambooPluginConfigRepositoryTest {

    private BambooPluginConfigRepository repository;

    @Mocked
    private StoreFactory storeFactory;
    @Mocked
    private Store<BambooPluginConfig> store;

    @Before
    public void setup() {
        new Expectations() {
            {
                storeFactory.getStore(BambooPluginConfig.class, "bamboo");
                returns(store);
            }
        };
        repository = new BambooPluginConfigRepository(storeFactory);
    }

    @Test
    public void testStoreConfig() {

        final BambooPluginConfig config = new BambooPluginConfig();

        new Expectations() {
            {
                store.set(config);
            }
        };

        repository.storeConfig(config);
    }

    @Test
    public void testGetConfig() {

        new Expectations() {
            {
                store.get();
                returns(new BambooPluginConfig());
            }
        };

        assertNotNull(repository.getConfig());
    }
}
