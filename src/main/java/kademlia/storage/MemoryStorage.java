package kademlia.storage;

import kademlia.node.Key;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MemoryStorage implements LocalStorage {

    private final ConcurrentHashMap<Key, Value> map = new ConcurrentHashMap<>();

    @Override
    public void put(Key key, Value value) {
        map.put(key, value);
    }

    @Override
    public Value get(Key key) {
        return map.get(key);
    }

    @Override
    public boolean contains(Key key) {
        return map.containsKey(key);
    }

    @Override
    public List<Key> getKeysBeforeTimestamp(long timestamp) {
        return map.entrySet().parallelStream().filter( entry ->
            entry.getValue().getLastPublished() <= timestamp
        ).map(entry -> entry.getKey()).collect(Collectors.toList());
    }
}
