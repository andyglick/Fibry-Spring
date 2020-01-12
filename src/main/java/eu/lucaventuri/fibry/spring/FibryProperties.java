package eu.lucaventuri.fibry.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fibry")
public class FibryProperties {
    private boolean forceDelivery = false;
    private String[] exposedActors = new String[0];
    private boolean privateIpOnly = true;
    private boolean debug = false;

    public boolean isForceDelivery() {
        return forceDelivery;
    }

    public void setForceDelivery(boolean forceDelivery) {
        this.forceDelivery = forceDelivery;
    }

    public String[] getExposedActors() {
        return exposedActors;
    }

    public void setExposedActors(String[] exposedActors) {
        this.exposedActors = exposedActors;
    }

    public boolean isPrivateIpOnly() {
        return privateIpOnly;
    }

    public void setPrivateIpOnly(boolean privateIpOnly) {
        this.privateIpOnly = privateIpOnly;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
