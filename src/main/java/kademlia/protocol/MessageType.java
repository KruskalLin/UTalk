package kademlia.protocol;

public enum MessageType {
    PING(Ping.class),
    STORE(Store.class),
    FIND_NODE(FindNode.class),
    FIND_VALUE(FindValue.class),
    PING_REPLY(PingReply.class),
    STORE_REPLY(StoreReply.class),
    NODE_REPLY(NodeReply.class),
    VALUE_REPLY(ValueReply.class),
    SEND_MESSAGE(SendMessage.class),
    MESSAGE_REPLY(MessageReply.class);

    private final Class msgClass;

    MessageType(Class msgClass) {
        this.msgClass = msgClass;
    }

    public Class getMsgClass() {
        return msgClass;
    }
}
