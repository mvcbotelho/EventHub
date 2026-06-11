package com.marcus.eventhub.registration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class RegistrationMetrics {

    private final Counter confirmedRegistrationsCounter;

    public RegistrationMetrics(MeterRegistry meterRegistry) {
        this.confirmedRegistrationsCounter = Counter.builder("eventhub.registrations.confirmed")
                .description("Confirmed event registrations")
                .register(meterRegistry);
    }

    public void trackConfirmed() {
        confirmedRegistrationsCounter.increment();
    }
}
