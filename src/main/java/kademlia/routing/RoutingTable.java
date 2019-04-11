package kademlia.routing;

import kademlia.client.KademliaClient;
import kademlia.node.Key;
import kademlia.node.Node;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ToString
@EqualsAndHashCode
public class RoutingTable {

    private final Key localNodeId;

    private final Bucket[] buckets;

    private final static int ID_LENGTH = 160;

    public RoutingTable(int k, Key localNodeId, KademliaClient client) {
        this.localNodeId = localNodeId;
        buckets = new Bucket[ID_LENGTH];
        for (int i = 0; i < ID_LENGTH; i++) {
            buckets[i] = new Bucket(client, k, i);
        }
    }

    /**
     * Compute the bucket ID in which a given node should be placed; the bucketId is computed based on how far the node is away from the Local Node.
     *
     * @param nid The Key for which we want to find which bucket it belong to
     *
     * @return Integer The bucket ID in which the given node should be placed.
     */
    public final int getBucketId(Key nid)
    {
        int bId = this.localNodeId.getDistance(nid) - 1;

        /* If we are trying to insert a node into it's own routing table, then the bucket ID will be -1, so let's just keep it in bucket 0 */
        return bId < 0 ? 0 : bId;
    }

    public void addNode(Node node) {
        if(!node.getId().equals(localNodeId)) {
            buckets[getBucketId(node.getId())].addNode(node);
        }
//        else {
//            throw new RuntimeException("Routing table of node=" + node.getId() + " can't contain itself. (localNodeId=" + localNodeId + ")");
//        }
    }

    public Bucket[] getBuckets() {
        return buckets;
    }

    public Stream<Bucket> getBucketStream() {
        return Arrays.stream(buckets);
    }

    public List<Node> findClosest(Key lookupId, int numberOfRequiredNodes) {
        return getBucketStream().flatMap(bucket -> bucket.getNodes().stream())
                .sorted(Comparator.comparing(node -> node.getId().getKey().xor(lookupId.getKey()).abs()))
                .limit(numberOfRequiredNodes).collect(Collectors.toList());
    }

    public void retireNode(Node node) {
        buckets[getBucketId(node.getId())].retireNode(node);
    }
}
