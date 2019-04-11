package kademlia.protocol;

import kademlia.node.Node;
import lombok.Data;

@Data
public abstract class Message {
    private final MessageType type;
    private final long seqId;
    private final Node origin;
}
