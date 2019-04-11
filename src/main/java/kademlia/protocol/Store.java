package kademlia.protocol;

import kademlia.node.Key;
import kademlia.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Store extends Message {
    private final String value;
    private final Key key;

    public Store(long seqId, Node origin, Key key, String value) {
        super(MessageType.STORE, seqId, origin);
        this.key = key;
        this.value = value;
    }
}
