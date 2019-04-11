package kademlia.exception;


public class UnknownListenerType extends RuntimeException{
    public UnknownListenerType(String url) {
        super("Can't parse url = " + url);
    }
}
