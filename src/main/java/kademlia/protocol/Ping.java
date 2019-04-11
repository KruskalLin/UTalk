package kademlia.protocol;

import kademlia.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Used to verify that a node is still alive.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Ping extends Message {
    public Ping(long seqId, Node origin) {
        super(MessageType.PING, seqId, origin);
    }
}
