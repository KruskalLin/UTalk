package kademlia.server;

import kademlia.exception.UnknownMessageType;
import kademlia.node.Key;
import kademlia.node.Node;
import kademlia.protocol.*;
import kademlia.routing.RoutingTable;
import kademlia.storage.LocalStorage;
import kademlia.storage.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class KademliaServerHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KademliaServerHandler.class);

    private final RoutingTable routingTable;
    private final int kValue;
    private final DatagramSocket socket;
    private Codec codec = new Codec();
    private final Node localNode;
    private final LocalStorage localStorage;

    public KademliaServerHandler(RoutingTable routingTable, LocalStorage localStorage,
                                 Node localNode, int kValue, DatagramSocket socket) {
        this.routingTable = routingTable;
        this.localNode = localNode;
        this.kValue = kValue;
        this.localStorage = localStorage;
        this.socket = socket;
    }

    public void channelRead(DatagramPacket packet, byte[] receiveData) throws Exception {
        Message message = codec.decode(receiveData);
        routingTable.addNode(message.getOrigin());
        LOGGER.debug("Received message type={},seqId={} from node={}", message.getType(),message.getSeqId(), message.getOrigin());
        if(message.getType() == MessageType.PING) {
            Ping ping = (Ping)message;
            routingTable.addNode(ping.getOrigin());
            respond(codec.encode(new PingReply(message.getSeqId(),localNode)), packet.getSocketAddress());
        }
        else if(message.getType() == MessageType.FIND_NODE) {
            FindNode findNode = (FindNode) message;
            List<Node> closest = routingTable.findClosest(findNode.getLookupId(), kValue);
            respond(codec.encode(new NodeReply(message.getSeqId(),localNode, closest)), packet.getSocketAddress());
        }
        else if(message.getType() == MessageType.FIND_VALUE) {
            FindValue findValue = (FindValue) message;

            // query local store
            if(localStorage.contains(findValue.getKey())) {
                respond(codec.encode(new ValueReply(message.getSeqId(),localNode, findValue.getKey(), localStorage.get(findValue.getKey()).getContent())), packet.getSocketAddress());
            }
            else {
                // Else send list of closest nodes
                List<Node> closest = routingTable.findClosest(new Key(findValue.getKey().hashCode()), kValue);
                respond(codec.encode(new NodeReply(message.getSeqId(),localNode, closest)), packet.getSocketAddress());
            }
        }
        else if(message.getType() == MessageType.STORE) {
            Store store = (Store) message;
            localStorage.put(store.getKey(), Value.builder().content(store.getValue()).lastPublished(System.currentTimeMillis()).build());
            respond(codec.encode(new StoreReply(message.getSeqId(),localNode)), packet.getSocketAddress());
        }
        else if(message.getType() == MessageType.SEND_MESSAGE) {
            SendMessage sendMessage = (SendMessage)message;
            /**
             * TODO: receive action
             */
            System.out.println(sendMessage.getValue());
            respond(codec.encode(new MessageReply(message.getSeqId(),localNode)), packet.getSocketAddress());
        }
        else {
            throw new UnknownMessageType();
        }
    }

    private void respond(byte[] encode, SocketAddress address) throws IOException {
        socket.send(new DatagramPacket(encode, encode.length, address));
    }
}
