package kademlia.routing;

import kademlia.client.KademliaClient;
import kademlia.storage.LocalStorage;
import kademlia.storage.Value;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.TimeoutException;

@Data
@Builder
public class KeyRepublishing {
    private final LocalStorage localStorage;
    private final KademliaClient kademliaClient;
    private final RoutingTable routingTable;
    private final int k;

    public void execute() {
        localStorage.getKeysBeforeTimestamp(System.currentTimeMillis() - 3600*1000)
        .stream().forEach( key -> {
            Value value = localStorage.get(key);
            routingTable.findClosest(key, k).stream().forEach( node -> {
                try {
                    kademliaClient.sendContentToNode(node, key, value.getContent());
                }
                catch (TimeoutException exp) {
                    routingTable.retireNode(node);
                }
            });
            localStorage.updateLastPublished(key, System.currentTimeMillis());
        });
    }
}
