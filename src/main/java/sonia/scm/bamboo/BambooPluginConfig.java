package sonia.scm.bamboo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class BambooPluginConfig {

    private final String url;
    private final String password;
    private final String username;
    private final boolean allowOverride;

    public BambooPluginConfig() {
        url = "";
        password = "";
        username = "";
        allowOverride = false;
    }

    public BambooPluginConfig(final String url, final String username, final String password, boolean allowOverride) {
        this.url = url;
        this.password = password;
        this.username = username;
        this.allowOverride = allowOverride;
    }

    public String getUrl() {
        return url;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAllowOverride() {
        return allowOverride;
    }
}
