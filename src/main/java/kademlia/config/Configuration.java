package kademlia.config;

import kademlia.listener.Listener;
import kademlia.node.Key;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;


@Data
@Builder
public class Configuration {

    private final Key nodeId;

    private final long getTimeoutMs;
    private final long networkTimeoutMs;
    private final int kValue;

    @Singular
    private final List<Listener> listeners;
    @Singular
    private final List<Listener> advertisedListeners;

    public static ConfigurationBuilder defaults() {
        return Configuration.builder()
                .getTimeoutMs(5000)
                .networkTimeoutMs(5000)
                .kValue(10);
    }
}
