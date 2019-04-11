package kademlia.protocol;

import kademlia.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class StoreReply extends Message {
    public StoreReply(long seqId, Node origin) {
        super(MessageType.STORE_REPLY, seqId, origin);
    }
}
