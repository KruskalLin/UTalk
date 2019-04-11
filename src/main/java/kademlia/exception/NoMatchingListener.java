package kademlia.exception;


public class NoMatchingListener extends RuntimeException {
    public NoMatchingListener() {
        super("Not matching.");
    }
}
