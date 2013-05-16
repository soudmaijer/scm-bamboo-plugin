package sonia.scm.bamboo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;

@Singleton
public class BambooPluginConfigRepository {

    private Store<BambooPluginConfig> store;

    @Inject
    public BambooPluginConfigRepository(StoreFactory storeFactory) {
        this.store = storeFactory.getStore(BambooPluginConfig.class, "bamboo");
    }

    /**
     * Saves the Crowd config.
     */
    public void storeConfig(BambooPluginConfig config) {
        store.set(config);
    }

    //~--- get methods ----------------------------------------------------------

    /**
     * Returns the Crowd config.
     *
     * @return the Crowd configuration.
     */
    public BambooPluginConfig getConfig() {
        return store.get();
    }
}
