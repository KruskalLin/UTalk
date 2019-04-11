package kademlia.protocol;

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
public class MessageReply extends Message {
    public MessageReply(long seqId, Node origin) {
        super(MessageType.MESSAGE_REPLY, seqId, origin);
    }
}
