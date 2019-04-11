package kademlia.node;

import kademlia.listener.Listener;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;

import java.util.List;


@Data
@EqualsAndHashCode(of={"id"})
@Builder
public class Node implements Comparable<Node>{
    private Key id;
    @Singular
    private final List<Listener> advertisedListeners;
    private long lastSeen = System.currentTimeMillis();

    @Override
    public int compareTo(Node o) {
        if (this.equals(o)) {
            return 0;
        }

        return (this.lastSeen > o.lastSeen) ? 1 : -1;
    }
}
