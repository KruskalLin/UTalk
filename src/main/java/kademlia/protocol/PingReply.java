package kademlia.protocol;

import kademlia.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PingReply extends Message {
    public PingReply(long seqId, Node origin) {
        super(MessageType.PING_REPLY, seqId, origin);
    }
}
