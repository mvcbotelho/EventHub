package com.marcus.eventhub.common.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eventhub.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private int authCapacity = 10;
    private int authRefillPerMinutes = 1;
    private int registrationCapacity = 30;
    private int registrationRefillPerMinutes = 1;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getAuthCapacity() {
        return authCapacity;
    }

    public void setAuthCapacity(int authCapacity) {
        this.authCapacity = authCapacity;
    }

    public int getAuthRefillPerMinutes() {
        return authRefillPerMinutes;
    }

    public void setAuthRefillPerMinutes(int authRefillPerMinutes) {
        this.authRefillPerMinutes = authRefillPerMinutes;
    }

    public int getRegistrationCapacity() {
        return registrationCapacity;
    }

    public void setRegistrationCapacity(int registrationCapacity) {
        this.registrationCapacity = registrationCapacity;
    }

    public int getRegistrationRefillPerMinutes() {
        return registrationRefillPerMinutes;
    }

    public void setRegistrationRefillPerMinutes(int registrationRefillPerMinutes) {
        this.registrationRefillPerMinutes = registrationRefillPerMinutes;
    }
}
