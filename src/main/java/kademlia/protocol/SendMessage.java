package kademlia.protocol;

import kademlia.node.Key;
import kademlia.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * All rights Reserved, Designed by Popping Lim
 *
 * @Author: Popping Lim
 * @Date: 2019/3/22
 * @Todo:
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SendMessage extends Message {
    private final String value;
    private final Key key;

    public SendMessage(long seqId, Node origin, Key key, String value) {
        super(MessageType.SEND_MESSAGE, seqId, origin);
        this.key = key;
        this.value = value;
    }
}
