package kademlia.protocol;


import kademlia.node.Key;
import kademlia.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Same as FIND_NODE, but if the recipient of the request has the requested key in its store, it will return the corresponding value.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ValueReply extends Message {
    private final String value;
    private final Key key;

    public ValueReply(long seqId, Node origin, Key key, String value) {
        super(MessageType.VALUE_REPLY, seqId, origin);
        this.key = key;
        this.value = value;
    }
}
