package kademlia.protocol;


import kademlia.node.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * The recipient of the request will return the k nodes in his own buckets that are the closest ones to the requested key.
 *
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FindNode extends Message {

    private final Key lookupId;

    public FindNode(long seqId, Node origin, Key lookupId) {
        super(MessageType.FIND_NODE, seqId, origin);
        this.lookupId = lookupId;
    }
}
