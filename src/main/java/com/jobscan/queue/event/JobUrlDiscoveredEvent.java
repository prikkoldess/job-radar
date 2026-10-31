package com.jobscan.queue.event;

import java.io.Serializable;

public record JobUrlDiscoveredEvent(
        String url,
        String source) implements Serializable {
}
