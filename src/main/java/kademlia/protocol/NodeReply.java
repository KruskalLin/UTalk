package kademlia.protocol;

import kademlia.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NodeReply extends Message {

    private final List<Node> nodes;

    public NodeReply(long seqId, Node origin, List<Node> nodes) {
        super(MessageType.NODE_REPLY, seqId, origin);
        this.nodes = nodes;
    }
}
