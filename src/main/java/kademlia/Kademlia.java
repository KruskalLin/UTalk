package kademlia;

import kademlia.client.KademliaClient;
import kademlia.config.Configuration;
import kademlia.listener.Listener;
import kademlia.listener.UDPListener;
import kademlia.node.Key;
import kademlia.node.Node;
import kademlia.protocol.ValueReply;
import kademlia.routing.KeyRepublishing;
import kademlia.routing.RoutingTable;
import kademlia.server.KademliaServer;
import kademlia.storage.LocalStorage;
import kademlia.storage.MemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;



public class Kademlia {
    private static final Logger LOGGER = LoggerFactory.getLogger(Kademlia.class);

    private final static int ID_LENGTH = 160;

    private final Node localNode;

    private final LocalStorage localStorage;

    private final RoutingTable routingTable;

    private final KademliaClient client;

    private final List<KademliaServer> servers = new ArrayList<>();

    private final Configuration config;

    public Kademlia(Key nodeId) {
        this(Configuration.defaults()
                .nodeId(nodeId)
                .build());
    }

    public Kademlia(Key nodeId, String listeners) {
        this(nodeId, listeners, new MemoryStorage());
    }


    public Kademlia(Key nodeId, String listeners, LocalStorage localStorage) {
        this(Configuration.defaults()
                .nodeId(nodeId)
                .listeners(
                        Arrays.stream(listeners.split(",")).map(Listener::fromUrl).collect(Collectors.toList())
                )
                .advertisedListeners(
                        Arrays.stream(listeners.split(",")).map(Listener::fromUrl).collect(Collectors.toList())
                )
                .build(), localStorage);
    }

    public Kademlia(Configuration config){
        this(config, new MemoryStorage());
    }

    public Kademlia(Configuration config, LocalStorage localStorage){
        this.config = config;
        this.localNode = Node.builder().id(config.getNodeId())
                .advertisedListeners(config.getAdvertisedListeners())
                .build();

        try {
            this.client = new KademliaClient(new DatagramSocket(), config, localNode);
        } catch (SocketException e) {
            throw new RuntimeException("Couldn't start client", e);
        }

        this.routingTable = new RoutingTable(config.getKValue(), config.getNodeId(), client);
        this.localStorage =  localStorage;

        config.getListeners().stream().filter(listener -> listener instanceof UDPListener)
                .map(listener -> (UDPListener)listener)
                .forEach( listener ->  {
                    try {
                        this.servers.add(new KademliaServer(listener.getHost(), listener.getPort(),
                                config.getKValue(), routingTable, localStorage, localNode));
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                });
    }

    public void bootstrap(Node bootstrapNode) throws TimeoutException{
        LOGGER.debug("bootstrapping node={}", localNode);

        client.sendPing(bootstrapNode, reply -> {
            LOGGER.debug("bootstrapping node={}, ping from remote={} received", localNode, bootstrapNode);
            routingTable.addNode(reply.getOrigin());
        });

        // FIND_NODE with own IDs to find nearby nodes
        client.sendFindNode(bootstrapNode, localNode.getId(), nodes -> {
            LOGGER.debug("bootstrapping node={}, sendFind node from remote={} received, nodes={}", localNode, bootstrapNode, nodes.size());
            nodes.stream().forEach(node -> routingTable.addNode(node));
        });

        LOGGER.debug("bootstrapping node={}, refreshing buckets", localNode);
        refreshBuckets();
    }


    /**
     *  send message
     */
    public void send(Key key, String value) throws TimeoutException{
        client.sendFindNode(localNode, key, nodes -> {
            nodes.stream().forEach(node -> {
               if(node.getId().equals(key)){
                   try {
                       client.sendMessageToNode(node, key ,value);
                   } catch (TimeoutException e) {
                       LOGGER.error("Put value time out!");
                       e.printStackTrace();
                   }
               }
            });
        });
    }

    /**
     * Put or Update the value in the DHT
     *
     * @param key
     * @param value
     */
    public void put(Key key, String value) throws TimeoutException{
        client.sendFindNode(localNode, key, nodes -> {
            nodes.stream().forEach(node -> {
                try {
                    client.sendContentToNode(node, key ,value);
                } catch (TimeoutException e) {
                    LOGGER.error("Put value time out!");
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     *
     * Retrieve the Value associated with the Key
     *
     * @param key
     * @return
     */
    public String get(Key key) throws TimeoutException {
        CompletableFuture<String> future = new CompletableFuture<>();
        new Thread(() -> {
            try {
                get(key, valueReply -> {
                    future.complete(valueReply.getValue());
                });
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }).start();
        try {
            return future.get(config.getGetTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new TimeoutException("Get value thread breaks up.");
        }
    }


    public void get(Key key, Consumer<ValueReply> valueReplyConsumer) throws TimeoutException {
        if(localStorage.contains(key)) {
            valueReplyConsumer.accept(new ValueReply(-1,localNode, key, localStorage.get(key).getContent()));
        }
        else {
            HashSet<Node> alreadyCheckedNodes = new HashSet<>();
            AtomicBoolean found = new AtomicBoolean(false);
            List<Node> nodes = routingTable.getBucketStream()
                    .flatMap(bucket -> bucket.getNodes().stream())
                    .sorted(Comparator.comparing(node -> node.getId().getKey().xor(key.getKey()).abs()))
                    .collect(Collectors.toList());

            get(found, key, nodes, alreadyCheckedNodes, valueReply -> {
                        if(!found.getAndSet(true)) {
                            valueReplyConsumer.accept(valueReply);
                        }
                    });
        }
    }

    private void get(AtomicBoolean found, Key key, List<Node> nodes, HashSet<Node> alreadyCheckedNodes, Consumer<ValueReply> valueReplyConsumer) throws TimeoutException {
        for( Node node : nodes) {
            if(!alreadyCheckedNodes.contains(node) && !found.get()) {
                client.sendFindValue(node,
                        key, nodeReply -> {
                            nodeReply.getNodes().stream().forEach(newNode -> routingTable.addNode(newNode));
                            try {
                                get(found, key, nodeReply.getNodes(), alreadyCheckedNodes, valueReplyConsumer);
                            } catch (TimeoutException e) {
                                e.printStackTrace();
                            }
                        }, valueReplyConsumer);

                alreadyCheckedNodes.add(node);
            }
        }
    }

    /**
     * Execute key republishing
     *
     * Iterate over all keys that weren't updated within the last hour and republish
     * to the other k-nodes that are closest to the associated keys
     */
    public void republishKeys() {
        KeyRepublishing.builder()
                .kademliaClient(client)
                .localStorage(localStorage)
                .routingTable(routingTable)
                .k(config.getKValue())
                .build().execute();
    }

    public void refreshBuckets() {
        // Refresh buckets
        for (int i = 1; i < ID_LENGTH; i++) {
            // Construct a Key that is i bits away from the current node Id
            final Key current = this.localNode.getId().generateNodeIdByDistance(i);
            routingTable.getBucketStream()
                    .flatMap(bucket -> bucket.getNodes().stream())
                    .forEach(node -> {
                        try {
                            client.sendFindNode(node, current, nodes -> {
                                nodes.stream().forEach(newNode -> routingTable.addNode(newNode));
                            });
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        }
                    });

        }
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public Node getLocalNode() {
        return localNode;
    }

    public void close() {
        servers.forEach(KademliaServer::close);
        client.close();
    }
}
