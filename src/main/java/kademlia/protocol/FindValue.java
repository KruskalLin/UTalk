package kademlia.protocol;

import kademlia.node.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * Same as FIND_NODE, but if the recipient of the request has the requested key in its store, it will return the corresponding value.
 *
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FindValue extends Message {

    private final Key key;

    public FindValue(long seqId, Node origin, Key key) {
        super(MessageType.FIND_VALUE, seqId, origin);
        this.key = key;
    }
}
