package kademlia.exception;


public class TimeoutException extends RuntimeException {

    public TimeoutException(Exception e) {
        super(e);
    }

    public TimeoutException() {

    }
}
