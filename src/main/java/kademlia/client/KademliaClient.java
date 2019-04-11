package kademlia.client;

import kademlia.config.Configuration;
import kademlia.exception.NoMatchingListener;
import kademlia.listener.ListenerType;
import kademlia.listener.UDPListener;
import kademlia.monitor.Monitor;
import kademlia.node.*;
import kademlia.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;


public class KademliaClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KademliaClient.class);

    private static SecureRandom random = new SecureRandom();

    private final KademliaClientHandler kademliaClientHandler;

    private final Configuration config;

    private final Node localNode;

    private final DatagramSocket socket;

    private Codec codec = new Codec();

    private final Duration timeout = Duration.ofSeconds(3);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Monitor monitor;

    public KademliaClient(DatagramSocket socket, Configuration config, Node localNode) {
        this.localNode = localNode;
        this.config = config;
        this.socket = socket;
        kademliaClientHandler = new KademliaClientHandler();
        try {
            monitor = new Monitor();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void send(Node node, long seqId, Message msg, Consumer<Message> consumer) throws TimeoutException, NoMatchingListener {
        kademliaClientHandler.registerHandler(seqId, consumer);
        UDPListener udpListener = node.getAdvertisedListeners().stream()
                .filter(listener -> listener.getType() == ListenerType.UDP)
                .map(listener -> (UDPListener) listener)
                .findFirst().orElseThrow(() -> new NoMatchingListener());

        // 3 tries
        Retry.builder()
                .interval(1000)
                .retries(3)
                .sender(() -> {
                    try {
                        byte[] payload = codec.encode(msg);
                        socket.send(new DatagramPacket(
                                payload,
                                payload.length,
                                new InetSocketAddress(udpListener.getHost(), udpListener.getPort())));

                        final Future future = executor.submit(new Callable() {
                            @Override
                            public String call() throws Exception {
                                byte[] receiveData = new byte[1024];
                                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                                socket.receive(receivePacket);
                                kademliaClientHandler.handle(receiveData, receivePacket);
                                return null;
                            }
                        });

                        try {
                            future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                        } catch (TimeoutException e) {
                            future.cancel(false);
                        }
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error("unsupported encoding for encoding msg", e);
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).build().execute();
    }

    public void sendPing(Node node, Consumer<PingReply> pongConsumer) throws TimeoutException {
        long seqId = random.nextLong();
        System.out.println(localNode.getId() + " PING " + node.getId());
        send(node, seqId, new Ping(seqId, localNode), msg -> {
            pongConsumer.accept((PingReply) msg);
        });
    }


    public void sendFindNode(Node node, Key key, Consumer<List<Node>> callback) throws TimeoutException {
        long seqId = random.nextLong();
        send(node, seqId, new FindNode(seqId, localNode, key),
                message -> {
                    NodeReply nodeReply = (NodeReply) message;
                    callback.accept(nodeReply.getNodes());
                }
        );
    }

    public void sendFindValue(Node node, Key key,
                              Consumer<NodeReply> nodeReplyConsumer, Consumer<ValueReply> valueReplyConsumer) throws TimeoutException {
        long seqId = random.nextLong();
        send(node, seqId, new FindValue(seqId, localNode, key),
                message -> {
                    if (message.getType() == MessageType.NODE_REPLY) {
                        NodeReply nodeReply = (NodeReply) message;
                         nodeReplyConsumer.accept(nodeReply);
                    }
                    else if (message.getType() == MessageType.VALUE_REPLY) {
                        ValueReply valueReply = (ValueReply) message;
                        valueReplyConsumer.accept(valueReply);
                    }
                }
        );
    }

    public void sendContentToNode(Node node, Key key, String value) throws TimeoutException {
        final long seqId = random.nextLong();
        send(node, seqId, new Store(seqId, localNode, key, value),
                message -> {

                }
        );
    }

    public void sendMessageToNode(Node node, Key key, String value) throws TimeoutException {
        final long seqId = random.nextLong();
        System.out.println(localNode.getId() + " 发送到 " + node.getId());
        send(node, seqId, new SendMessage(seqId, localNode, key, value),
                message -> {
                }
        );

        try {
            monitor.message(localNode.getId().toString(), node.getId().toString(), "SEND");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        executor.shutdownNow();
        socket.close();
    }
}
